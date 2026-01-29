package compilation

import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

///////////////////////////////////////////////////////////////////////////
// Common
///////////////////////////////////////////////////////////////////////////

/**
 * Compile-time options for SQLite compilation.
 */
val SqliteCompileTimeOptions = arrayOf(
    "-DCODEC_TYPE=CODEC_TYPE_CHACHA20",
    "-DSQLITE_ENABLE_FTS5=1",
    "-DSQLITE_ENABLE_JSON1=1",
    "-DSQLITE_ENABLE_RTREE=1"
)

/**
 * Returns the sqlite header file (.h).
 */
fun sqliteHeaderFile(
    sources: DirectoryProperty,
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return sources.zip(params) { directory, params ->
        directory.file("${params.sqliteMcName}.h")
    }
}

/**
 * Returns the sqlite source file (.c).
 */
fun sqliteSourceFile(
    sources: DirectoryProperty,
    params: Provider<SqliteCompilationParameters>
): Provider<RegularFile> {
    return sources.zip(params) { directory, params ->
        directory.file("${params.sqliteMcName}.c")
    }
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
    platformFlag: String
): List<String> {
    val sdkDir = sdk.replaceFirstChar { it.uppercase() }

    val sdkPath = "/Applications/Xcode.app/Contents/Developer/Platforms/$sdkDir.platform" +
            "/Developer/SDKs/$sdkDir.sdk"

    return listOf("-arch", arch, "-isysroot", sdkPath, platformFlag)
}

/**
 * Returns the compiler flags for SQLite compilation for Kotlin native [target].
 */
fun SqliteCompilationParameters.getNativeCompilerFlags(target: KonanTarget): List<String> {
    return when (target.family) {
        Family.OSX -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported macOS architecture: ${target.architecture}")
            }

            listOf("-arch", arch, "-mmacosx-version-min=$macosVersionMin")
        }

        Family.IOS -> xcodeCompilerFlags(
            arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported iOS architecture: ${target.architecture}")
            },
            sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "iphonesimulator"
                else -> "iphoneos"
            },
            platformFlag = "-mios-version-min=$iosVersionMin"
        )

        Family.TVOS -> xcodeCompilerFlags(
            arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported tvOS architecture: ${target.architecture}")
            },
            sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "appletvsimulator"
                else -> "appletvos"
            },
            platformFlag = "-mtvos-version-min=$tvosVersionMin"
        )

        Family.WATCHOS -> xcodeCompilerFlags(
            arch = when (target.architecture) {
                Architecture.ARM64 -> when (target.name) {
                    "watchosDeviceArm64" -> "arm64_32"
                    else -> "arm64"
                }

                Architecture.ARM32 -> "armv7k"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported watchOS architecture: ${target.architecture}")
            },
            sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "watchsimulator"
                else -> "watchos"
            },
            platformFlag = "-mwatchos-version-min=$watchosVersionMin"
        )

        Family.LINUX -> {
            val targetTriple = when (target.architecture) {
                Architecture.X64 -> "x86_64-unknown-linux-gnu"
                Architecture.ARM64 -> "aarch64-unknown-linux-gnu"
                else -> error("Unsupported Linux architecture: ${target.architecture}")
            }

            listOf("--target=$targetTriple")
        }

        Family.MINGW -> {
            // Only mingwX64 in your targets
            listOf("-target", "x86_64-w64-mingw32")
        }

        Family.ANDROID -> {
            val targetTriple = when (target.architecture) {
                Architecture.ARM64 -> "aarch64-linux-android"
                Architecture.ARM32 -> "armv7a-linux-androideabi"
                Architecture.X64 -> "x86_64-linux-android"
                Architecture.X86 -> "i686-linux-android"
            }

            listOf(
                "-target", "$targetTriple$androidSdkMin",
                "--sysroot=$androidNdkToolchainPath/sysroot"
            )
        }
    }
}

/**
 * Returns the compiler name for Kotlin native [target].
 */
fun SqliteCompilationParameters.getNativeCompiler(target: KonanTarget): String {
    return when (target.family) {
        Family.OSX, Family.IOS, Family.TVOS, Family.WATCHOS -> "clang"
        Family.LINUX -> "clang"
        Family.MINGW -> "x86_64-w64-mingw32-gcc"
        Family.ANDROID -> "$androidNdkToolchainPath/bin/clang"
    }
}

fun SqliteCompilationParameters.getNativeArchiver(target: KonanTarget): String {
    return when (target.family) {
        Family.MINGW -> "x86_64-w64-mingw32-ar"
        Family.ANDROID -> "$androidNdkToolchainPath/bin/llvm-ar"
        else -> "ar"
    }
}