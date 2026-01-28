import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget

///////////////////////////////////////////////////////////////////////////
// Common flags
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

///////////////////////////////////////////////////////////////////////////
// Native
///////////////////////////////////////////////////////////////////////////

/**
 * Returns absolute path of the android NDK directory.
 */
private inline val SqliteCompilerExtension.androidNdkPath: String
    get() = androidNdkDirectory.get().asFile.absolutePath

/**
 * Returns the path for [sdk] in the Xcode application.
 */
private fun xcodeSdkPath(sdk: String): String {
    val sdkDir = sdk.replaceFirstChar { it.uppercase() }

    return "/Applications/Xcode.app/Contents/Developer/Platforms/$sdkDir.platform" +
            "/Developer/SDKs/$sdkDir.sdk"
}

/**
 * Returns the compiler flags for SQLite compilation for Kotlin native [target].
 */
fun SqliteCompilerExtension.getNativeCompilerFlags(target: KonanTarget): List<String> {
    return when (target.family) {
        Family.OSX -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported macOS architecture: ${target.architecture}")
            }
            listOf("-arch", arch, "-mmacosx-version-min=10.13")
        }

        Family.IOS -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported iOS architecture: ${target.architecture}")
            }

            val sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "iphonesimulator"
                else -> "iphoneos"
            }

            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mios-version-min=12.0")
        }

        Family.TVOS -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> "arm64"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported tvOS architecture: ${target.architecture}")
            }

            val sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "appletvsimulator"
                else -> "appletvos"
            }

            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mtvos-version-min=12.0")
        }

        Family.WATCHOS -> {
            val arch = when (target.architecture) {
                Architecture.ARM64 -> when (target.name) {
                    "watchosDeviceArm64" -> "arm64_32"
                    else -> "arm64"
                }

                Architecture.ARM32 -> "armv7k"
                Architecture.X64 -> "x86_64"
                else -> error("Unsupported watchOS architecture: ${target.architecture}")
            }

            val sdk = when {
                target.name.contains("simulator", ignoreCase = true) -> "watchsimulator"
                else -> "watchos"
            }

            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mwatchos-version-min=5.0")
        }

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
            val (targetTriple, apiLevel) = when (target.architecture) {
                Architecture.ARM64 -> "aarch64-linux-android" to 21
                Architecture.ARM32 -> "armv7a-linux-androideabi" to 21
                Architecture.X64 -> "x86_64-linux-android" to 21
                Architecture.X86 -> "i686-linux-android" to 21
            }

            listOf(
                "-target", "$targetTriple$apiLevel",
                "--sysroot=$androidNdkDirectory/toolchains/llvm/prebuilt/darwin-x86_64/sysroot"
            )
        }
    }
}

/**
 * Returns the compiler name for Kotlin native [target].
 */
fun SqliteCompilerExtension.getNativeCompiler(target: KonanTarget): String {
    return when (target.family) {
        Family.OSX, Family.IOS, Family.TVOS, Family.WATCHOS -> "clang"
        Family.LINUX -> "clang"
        Family.MINGW -> "x86_64-w64-mingw32-gcc"
        Family.ANDROID -> "$androidNdkPath/toolchains/llvm/prebuilt/darwin-x86_64/bin/clang"
    }
}

fun SqliteCompilerExtension.getNativeArchiver(target: KonanTarget): String {
    return when (target.family) {
        Family.MINGW -> "x86_64-w64-mingw32-ar"
        Family.ANDROID -> "$androidNdkPath/toolchains/llvm/prebuilt/darwin-x86_64/bin/llvm-ar"
        else -> "ar"
    }
}