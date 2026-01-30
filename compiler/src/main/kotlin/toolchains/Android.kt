package toolchains

import KsqliteChecksums
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import java.io.File

/**
 * Returns the Android NDK download URL or null if the NDK is not supported on the current platform.
 */
fun androidNdkDownloadFileName(version: String): String? {
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

    return "android-ndk-${version.substringBefore('.')}-$platform.$extension"
}

/**
 * Returns the checksum for the Android NDK and the current operating system.
 */
fun KsqliteChecksums.androidNdk(): String = when (Host.Current.operatingSystem) {
    Host.OperatingSystem.MacOS -> androidNdkMacos
    Host.OperatingSystem.Windows -> androidNdkWindows
    Host.OperatingSystem.Linux -> androidNdkLinux
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
 * Returns the path to the android NDK directory from the root [toolchainsDirectory].
 */
fun androidNdkDirectory(toolchainsDirectory: Provider<Directory>): Provider<Directory> {
    //"$ndkDir/toolchains/llvm/prebuilt/$ndkHostTag"
    return toolchainsDirectory.map { it.dir(TOOLCHAIN_ANDROID_NDK) }
}

/**
 * Returns a file tree to the downloaded Android NDK.
 */
fun Task.androidNdkExtract(
    version: Provider<String>,
    downloadedFile: Provider<File>
): FileTree = when (Host.Current.operatingSystem) {
    Host.OperatingSystem.Windows, Host.OperatingSystem.Linux -> project.zipTree(downloadedFile)
    Host.OperatingSystem.MacOS -> {
        val execOperations = project.serviceOf<ExecOperations>()

        doFirst {
            execOperations.exec {
                commandLine("hdiutil", "attach", downloadedFile.get().absolutePath)
            }.rethrowFailure()
        }

        doLast {
            val versionMajor = version.get().substringBefore('.')

            execOperations.exec {
                commandLine("hdiutil", "detach", "/Volumes/Android NDK $versionMajor")
            }.rethrowFailure()
        }

        project.fileTree(version.map { value ->
            val (major, build) = value.split('.')
            "/Volumes/Android NDK $major/AndroidNDK$build.app/Contents/NDK"
        })
    }
}