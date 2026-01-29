import org.gradle.internal.os.OperatingSystem
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
 * Tag of the host for android NDK.
 */
private val ndkHostTag: String by lazy {
    OperatingSystem.current().run {
        when {
            isWindows -> "windows-x86_64"
            isMacOsX -> "darwin-x86_64"
            isLinux -> "linux-x86_64"
            else -> throw UnsupportedOperationException("Unsupported operation system $this")
        }
    }
}

/**
 * Returns absolute path of the android NDK directory.
 */
private inline val SqliteCompilerExtension.ndkToolchain: String
    get() = "${androidNdkDirectory.get().asFile.absolutePath}/toolchains/llvm/prebuilt/$ndkHostTag"

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

            listOf("-arch", arch, "-mmacosx-version-min=${macosVersionMin.get()}")
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

            val version = iosVersionMin.get()
            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mios-version-min=$version")
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

            val version = tvosVersionMin.get()
            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mtvos-version-min=$version")
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

            val version = watchosVersionMin.get()
            listOf("-arch", arch, "-isysroot", xcodeSdkPath(sdk), "-mwatchos-version-min=$version")
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
            val targetTriple = when (target.architecture) {
                Architecture.ARM64 -> "aarch64-linux-android"
                Architecture.ARM32 -> "armv7a-linux-androideabi"
                Architecture.X64 -> "x86_64-linux-android"
                Architecture.X86 -> "i686-linux-android"
            }

            listOf(
                "-target", "$targetTriple${androidSdkMin.get()}",
                "--sysroot=$ndkToolchain/sysroot"
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
        Family.ANDROID -> "$ndkToolchain/bin/clang"
    }
}

fun SqliteCompilerExtension.getNativeArchiver(target: KonanTarget): String {
    return when (target.family) {
        Family.MINGW -> "x86_64-w64-mingw32-ar"
        Family.ANDROID -> "$ndkToolchain/bin/llvm-ar"
        else -> "ar"
    }
}