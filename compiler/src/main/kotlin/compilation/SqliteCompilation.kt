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
// Executables
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the compiler executable arguments for [operatingSystem].
 */
fun compiler(
    operatingSystem: OperatingSystem,
    params: SqliteCompilationParameters
): Array<String> {
    return when (operatingSystem) {
        is OperatingSystem.Darwin -> arrayOf("clang")
        OperatingSystem.Android -> arrayOf(params.androidToolchainPath("bin/clang"))
        OperatingSystem.Linux -> arrayOf("zig", "cc")
        OperatingSystem.Windows -> arrayOf("x86_64-w64-mingw32-gcc")
    }
}

/**
 * Returns the archiver executable arguments for [operatingSystem].
 */
fun archiver(
    operatingSystem: OperatingSystem,
    params: SqliteCompilationParameters
): Array<String> {
    return when (operatingSystem) {
        is OperatingSystem.Darwin -> arrayOf("ar")
        OperatingSystem.Android -> arrayOf(params.androidToolchainPath("bin/llvm-ar"))
        OperatingSystem.Linux -> arrayOf("zig", "ar")
        OperatingSystem.Windows -> arrayOf("x86_64-w64-mingw32-ar")
    }
}

///////////////////////////////////////////////////////////////////////////
// Shared library
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the compiler flags for SQLite shared library compilation for [platform].
 */
fun sharedCompilerFlags(
    params: SqliteCompilationParameters,
    platform: Platform
): Array<String> = when (val os = platform.operatingSystem) {
    OperatingSystem.MacOS -> {
        val arch = when (val architecture = platform.architecture) {
            Architecture.Arm64 -> "arm64"
            Architecture.X64 -> "x86_64"
            else -> error("Unsupported macOS architecture: $architecture")
        }

        arrayOf("-dynamiclib", "-arch", arch)
    }

    OperatingSystem.Linux -> {
        val targetTriple = when (val architecture = platform.architecture) {
            Architecture.X64 -> "x86_64-linux-gnu"
            Architecture.Arm64 -> "aarch64-linux-gnu"
            else -> error("Unsupported Linux architecture: $architecture")
        }

        arrayOf("-target", targetTriple)
    }

    OperatingSystem.Windows -> arrayOf("-shared")

    else -> error("Unsupported operating system for shared library: $os")
}

///////////////////////////////////////////////////////////////////////////
// Static library
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the compiler flags for Xcode compilation.
 */
@Suppress("NewApi")
private fun xcodeCompilerFlags(
    arch: String,
    sdk: String,
    flag: String,
    execOperations: ExecOperations
): Array<String> {
    val sdkPath = ByteArrayOutputStream().use { output ->
        execOperations.exec {
            standardOutput = output
            commandLine("xcrun", "--sdk", sdk, "--show-sdk-path")
        }.rethrowFailure()

        output.toString(Charsets.UTF_8).trimEnd()
    }

    return arrayOf("-arch", arch, "-isysroot", sdkPath, flag)
}

/**
 * Returns the compiler flags for SQLite static library compilation for [platform].
 */
fun staticCompilerFlags(
    execOperations: ExecOperations,
    params: SqliteCompilationParameters,
    platform: Platform
): Array<String> = when (val os = platform.operatingSystem) {
    OperatingSystem.MacOS -> xcodeCompilerFlags(
        arch = when (val architecture = platform.architecture) {
            Architecture.Arm64 -> "arm64"
            Architecture.X64 -> "x86_64"
            else -> error("Unsupported macOS architecture: $architecture")
        },
        sdk = "macosx",
        flag = "-mmacosx-version-min=${params.macosVersionMin}",
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
        flag = "-mios-version-min=${params.iosVersionMin}",
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
        flag = "-mtvos-version-min=${params.tvosVersionMin}",
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
        flag = "-mwatchos-version-min=${params.watchosVersionMin}",
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
            "-target", "$targetTriple${params.androidSdkMin}",
            "--sysroot=${params.androidToolchainPath("sysroot")}"
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