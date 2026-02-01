package compilation

import org.gradle.process.ExecOperations
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
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
// Shared
///////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////
// Static
///////////////////////////////////////////////////////////////////////////

/**
 * Resolves [path] relatively to the Android NDK toolchain directory and returns the absolute path
 * of the resolved file.
 */
private fun SqliteCompilationParameters.androidToolchainPath(path: String): String {
    return "${toolchains.android.path}/toolchains/llvm/prebuilt/${androidNdkHostTag()}/$path"
}

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
 * Returns the compiler flags for SQLite compilation for Kotlin native [target].
 */
fun SqliteCompilationParameters.getNativeCompilerFlags(
    target: KonanTarget,
    execOperations: ExecOperations
): Array<String> = when (target.family) {
    Family.OSX -> xcodeCompilerFlags(
        arch = when (target.architecture) {
            Architecture.ARM64 -> "arm64"
            Architecture.X64 -> "x86_64"
            else -> error("Unsupported macOS architecture: ${target.architecture}")
        },
        sdk = "macosx",
        flag = "-mmacosx-version-min=$macosVersionMin",
        execOperations = execOperations
    )

    Family.IOS -> xcodeCompilerFlags(
        arch = when (target.architecture) {
            Architecture.ARM64 -> "arm64"
            Architecture.X64 -> "x86_64"
            else -> error("Unsupported iOS architecture: ${target.architecture}")
        },
        sdk = when (target) {
            KonanTarget.IOS_ARM64 -> "iphoneos"
            KonanTarget.IOS_SIMULATOR_ARM64, KonanTarget.IOS_X64 -> "iphonesimulator"
            else -> error("Unsupported target for iOS family: $target")
        },
        flag = "-mios-version-min=$iosVersionMin",
        execOperations = execOperations
    )

    Family.TVOS -> xcodeCompilerFlags(
        arch = when (target.architecture) {
            Architecture.ARM64 -> "arm64"
            Architecture.X64 -> "x86_64"
            else -> error("Unsupported tvOS architecture: ${target.architecture}")
        },
        sdk = when (target) {
            KonanTarget.TVOS_ARM64 -> "appletvos"
            KonanTarget.TVOS_SIMULATOR_ARM64, KonanTarget.TVOS_X64 -> "appletvsimulator"
            else -> error("Unsupported target for tvOS family: $target")
        },
        flag = "-mtvos-version-min=$tvosVersionMin",
        execOperations = execOperations
    )

    Family.WATCHOS -> xcodeCompilerFlags(
        arch = when (target.architecture) {
            Architecture.ARM32 -> "armv7k"
            Architecture.X64 -> "x86_64"

            Architecture.ARM64 -> when (target) {
                KonanTarget.WATCHOS_ARM64 -> "arm64_32"
                KonanTarget.WATCHOS_DEVICE_ARM64, KonanTarget.WATCHOS_SIMULATOR_ARM64 -> "arm64"
                else -> error("Unsupported target for watchOS family: $target")
            }

            else -> error("Unsupported watchOS architecture: ${target.architecture}")
        },
        sdk = when (target) {
            KonanTarget.WATCHOS_ARM32,
            KonanTarget.WATCHOS_ARM64,
            KonanTarget.WATCHOS_DEVICE_ARM64 -> "watchos"

            KonanTarget.WATCHOS_SIMULATOR_ARM64, KonanTarget.WATCHOS_X64 -> "watchsimulator"
            else -> error("Unsupported target for watchOS family: $target")
        },
        flag = "-mwatchos-version-min=$watchosVersionMin",
        execOperations = execOperations
    )

    Family.LINUX -> {
        val targetTriple = when (target.architecture) {
            Architecture.X64 -> "x86_64-linux-gnu"
            Architecture.ARM64 -> "aarch64-linux-gnu"
            else -> error("Unsupported Linux architecture: ${target.architecture}")
        }

        arrayOf("-target", targetTriple)
    }

    Family.MINGW -> arrayOf("-target", "x86_64-w64-mingw32")

    Family.ANDROID -> {
        val targetTriple = when (target.architecture) {
            Architecture.ARM64 -> "aarch64-linux-android"
            Architecture.ARM32 -> "armv7a-linux-androideabi"
            Architecture.X64 -> "x86_64-linux-android"
            Architecture.X86 -> "i686-linux-android"
        }

        arrayOf(
            "-target", "$targetTriple$androidSdkMin",
            "--sysroot=${androidToolchainPath("sysroot")}"
        )
    }
}

/**
 * Returns the compiler name for Kotlin native [target].
 */
fun SqliteCompilationParameters.getNativeCompilerArgs(target: KonanTarget): Array<String> {
    return when (target.family) {
        Family.OSX, Family.IOS, Family.TVOS, Family.WATCHOS -> arrayOf("clang")
        Family.ANDROID -> arrayOf(androidToolchainPath("bin/clang"))
        Family.LINUX -> arrayOf("zig", "cc")
        Family.MINGW -> arrayOf("x86_64-w64-mingw32-gcc")
    }
}

fun SqliteCompilationParameters.getNativeArchiverArgs(target: KonanTarget): Array<String> {
    return when (target.family) {
        Family.OSX, Family.IOS, Family.TVOS, Family.WATCHOS -> arrayOf("ar")
        Family.ANDROID -> arrayOf(androidToolchainPath("bin/llvm-ar"))
        Family.LINUX -> arrayOf("zig", "ar")
        Family.MINGW -> arrayOf("x86_64-w64-mingw32-ar")
    }
}