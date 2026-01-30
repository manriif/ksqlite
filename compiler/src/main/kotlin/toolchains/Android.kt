package toolchains

import KsqliteChecksums
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import utils.copyFirstDirectoryContent

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
 * Returns a file tree to the downloaded Android NDK.
 */
fun Task.androidNdkExtract(
    version: Provider<String>,
    fileOperations: FileSystemOperations,
    downloadedFile: Provider<RegularFile>,
    destination: Provider<Directory>
): Task = when (Host.Current.operatingSystem) {
    Host.OperatingSystem.Windows, Host.OperatingSystem.Linux -> {
        val sources = project.zipTree(downloadedFile)

        doLast {
            fileOperations.copyFirstDirectoryContent(
                source = sources,
                destination = destination
            )
        }
    }

    Host.OperatingSystem.MacOS -> {
        val execOperations = project.serviceOf<ExecOperations>()

        val ndkSources = version.map { value ->
            val (major, build) = value.split('.')
            "/Volumes/Android NDK $major/AndroidNDK$build.app/Contents/NDK"
        }

        doLast {
            val versionMajor = version.get().substringBefore('.')

            execOperations.exec {
                commandLine("hdiutil", "attach", downloadedFile.get().asFile.absolutePath)
            }.rethrowFailure()

            try {
                fileOperations.copy {
                    from(ndkSources)
                    into(destination)
                }
            } finally {
                execOperations.exec {
                    commandLine("hdiutil", "detach", "/Volumes/Android NDK $versionMajor")
                }.rethrowFailure()
            }
        }
    }
}