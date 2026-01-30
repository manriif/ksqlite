package toolchains

import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

/**
 * Returns the Android NDK download URL or null if the NDK is not supported on the current platform.
 */
fun androidNdkDownloadUrl(version: String): String? {
    val (platform, extension) = when (Host.Current.operatingSystem) {
        Host.OperatingSystem.MacOS -> "darwin" to "dmg"

        Host.OperatingSystem.Windows -> when (Host.Current.architecture) {
            Host.Architecture.Arm64 -> return null
            Host.Architecture.X86_64 -> "windows" to "zip"
        }

        Host.OperatingSystem.Linux -> when (Host.Current.architecture) {
            Host.Architecture.Arm64 -> return null
            Host.Architecture.X86_64 -> "linux" to "zip"
        }
    }

    return "https://dl.google.com/android/repository/android-ndk-$version-$platform.$extension"
}

/**
 * Returns the Android NDK host tag.
 */
fun androidNdkHostTag(): String = when (Host.Current.operatingSystem) {
    Host.OperatingSystem.MacOS -> "darwin-x86_64"
    Host.OperatingSystem.Windows -> "windows-x86_64"
    Host.OperatingSystem.Linux -> "linux-x86_64"
}

/**
 * Returns the path to the android NDK directory.
 */
fun androidNdkDirectory(toolchainDirectory: Provider<Directory>): Provider<Directory> {
    //"$ndkDir/toolchains/llvm/prebuilt/$ndkHostTag"
    return toolchainDirectory.map { it.dir(TOOLCHAIN_ANDROID_NDK) }
}