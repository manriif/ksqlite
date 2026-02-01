package compilation

import org.gradle.process.ExecOperations
import platform.Architecture
import platform.OperatingSystem
import platform.Platform
import toolchains.androidNdkHostTag
import java.io.ByteArrayOutputStream

///////////////////////////////////////////////////////////////////////////
// Common
///////////////////////////////////////////////////////////////////////////

/**
 * Compile-time options for SQLite compilation.
 */
val SqliteCompileTimeOptions = arrayOf(
    "CODEC_TYPE=CODEC_TYPE_CHACHA20",
    "SQLITE_ENABLE_FTS5=1",
    "SQLITE_ENABLE_JSON1=1",
    "SQLITE_ENABLE_RTREE=1"
)

///////////////////////////////////////////////////////////////////////////
// Toolchains
///////////////////////////////////////////////////////////////////////////

/**
 * Resolves [path] relatively to the Android NDK toolchain directory and returns the absolute path
 * of the resolved file.
 */
private fun SqliteCompilationParameters.androidToolchainPath(path: String): String {
    return "${toolchains.android.path}/toolchains/llvm/prebuilt/${androidNdkHostTag()}/$path"
}

///////////////////////////////////////////////////////////////////////////
// Shared
///////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////
// Static
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the compiler flags for Xcode compilation.
 */
private fun xcodeCompilerFlags(
    arch: String,
    sdk: String,
    flag: String,
    execOperations: ExecOperations
): Array<String> {
    val sdkOutputStream = ByteArrayOutputStream()

    execOperations.exec {
        standardOutput = sdkOutputStream
        commandLine("xcrun", "--sdk", sdk, "--show-sdk-path")
    }.rethrowFailure()

    val sdkPath = sdkOutputStream.toByteArray().toString(Charsets.US_ASCII)

    return arrayOf("-arch", arch, "-isysroot", sdkPath, flag)
}

/**
 * Returns the compiler flags for SQLite compilation for Kotlin native [platform].
 */
fun SqliteCompilationParameters.getNativeCompilerFlags(
    execOperations: ExecOperations,
    platform: Platform
): Array<String> = when (val os = platform.operatingSystem) {
    OperatingSystem.MacOS -> xcodeCompilerFlags(
        arch = when (val architecture = platform.architecture) {
            Architecture.Arm64 -> "arm64"
            Architecture.X64 -> "x86_64"
            else -> error("Unsupported macOS architecture: $architecture")
        },
        sdk = "macosx",
        flag = "-mmacosx-version-min=$macosVersionMin",
        execOperations = execOperations
    )

    is OperatingSystem.IOS -> xcodeCompilerFlags(
        arch = when (val architecture = platform.architecture) {
            Architecture.Arm64 -> "arm64"
            Architecture.X64 -> "x86_64"
            else -> error("Unsupported iOS architecture: $architecture")
        },
        sdk = when (os) {
            OperatingSystem.IOS.Device -> "iphoneos"
            OperatingSystem.IOS.Simulator -> "iphonesimulator"
        },
        flag = "-mios-version-min=$iosVersionMin",
        execOperations = execOperations
    )

    is OperatingSystem.TvOS -> xcodeCompilerFlags(
        arch = when (val architecture = platform.architecture) {
            Architecture.Arm64 -> "arm64"
            Architecture.X64 -> "x86_64"
            else -> error("Unsupported tvOS architecture: $architecture")
        },
        sdk = when (os) {
            OperatingSystem.TvOS.Device -> "appletvos"
            OperatingSystem.TvOS.Simulator -> "appletvsimulator"
        },
        flag = "-mtvos-version-min=$tvosVersionMin",
        execOperations = execOperations
    )

    is OperatingSystem.WatchOS -> xcodeCompilerFlags(
        arch = when (val architecture = platform.architecture) {
            Architecture.Arm32 -> "armv7k"
            Architecture.X64 -> "x86_64"

            Architecture.Arm64 -> when (os) {
                OperatingSystem.WatchOS.Device -> "arm64_32"
                OperatingSystem.WatchOS.DeviceGen2, OperatingSystem.WatchOS.Simulator -> "arm64"
            }

            else -> error("Unsupported watchOS architecture: $architecture")
        },
        sdk = when (os) {
            OperatingSystem.WatchOS.Device, OperatingSystem.WatchOS.DeviceGen2 -> "watchos"
            OperatingSystem.WatchOS.Simulator -> "watchsimulator"
        },
        flag = "-mwatchos-version-min=$watchosVersionMin",
        execOperations = execOperations
    )

    OperatingSystem.Android -> {
        val targetTriple = when (platform.architecture) {
            Architecture.Arm64 -> "aarch64-linux-android"
            Architecture.Arm32 -> "armv7a-linux-androideabi"
            Architecture.X64 -> "x86_64-linux-android"
            Architecture.X86 -> "i686-linux-android"
        }

        arrayOf(
            "-target", "$targetTriple$androidSdkMin",
            "--sysroot=${androidToolchainPath("sysroot")}"
        )
    }

    OperatingSystem.Linux -> {
        val targetTriple = when (val architecture = platform.architecture) {
            Architecture.X64 -> "x86_64-linux-gnu"
            Architecture.Arm64 -> "aarch64-linux-gnu"
            else -> error("Unsupported Linux architecture: $architecture")
        }

        arrayOf("-target", targetTriple)
    }

    OperatingSystem.Windows -> arrayOf("-target", "x86_64-w64-mingw32")
}

/**
 * Returns the compiler name for Kotlin native [target].
 */
fun SqliteCompilationParameters.getNativeCompilerArgs(platform: Platform): Array<String> {
    return when (platform.operatingSystem) {
        is OperatingSystem.Darwin -> arrayOf("clang")
        OperatingSystem.Android -> arrayOf(androidToolchainPath("bin/clang"))
        OperatingSystem.Linux -> arrayOf("zig", "cc")
        OperatingSystem.Windows -> arrayOf("x86_64-w64-mingw32-gcc")
    }
}

fun SqliteCompilationParameters.getNativeArchiverArgs(platform: Platform): Array<String> {
    return when (platform.operatingSystem) {
        is OperatingSystem.Darwin -> arrayOf("ar")
        OperatingSystem.Android -> arrayOf(androidToolchainPath("bin/llvm-ar"))
        OperatingSystem.Linux -> arrayOf("zig", "ar")
        OperatingSystem.Windows -> arrayOf("x86_64-w64-mingw32-ar")
    }
}