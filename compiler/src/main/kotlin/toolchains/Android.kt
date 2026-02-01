package toolchains

import KsqliteChecksums
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import platform.Architecture
import platform.Host
import platform.OperatingSystem
import utils.copyFirstDirectoryContent

///////////////////////////////////////////////////////////////////////////
// Download
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the Android NDK download URL or null if the NDK is not supported on the current platform.
 */
fun androidNdkDownloadUrl(version: String): String? {
    val (platform, extension) = Host.Current.run {
        when (operatingSystem) {
            OperatingSystem.Linux -> when (architecture) {
                Architecture.Arm64 -> return null
                Architecture.X64 -> "linux" to "zip"
            }

            OperatingSystem.MacOS -> "darwin" to "dmg"

            OperatingSystem.Windows -> when (architecture) {
                Architecture.Arm64 -> return null
                Architecture.X64 -> "windows" to "zip"
            }
        }
    }

    val major = version.substringBefore('.')

    return "https://dl.google.com/android/repository/android-ndk-${major}-$platform.$extension"
}

/**
 * Extracts the Android NDK from [downloadedFile] into [destination].
 */
fun Task.androidNdkExtract(
    version: Provider<String>,
    fileOperations: FileSystemOperations,
    downloadedFile: Provider<RegularFile>,
    destination: Provider<Directory>
) {
    when (Host.Current.operatingSystem) {
        OperatingSystem.Windows, OperatingSystem.Linux -> {
            val sources = project.zipTree(downloadedFile)

            doLast {
                fileOperations.copyFirstDirectoryContent(
                    source = sources,
                    destination = destination
                )
            }
        }

        OperatingSystem.MacOS -> {
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
}

/**
 * Returns the checksum for the Android NDK and the current operating system.
 */
fun KsqliteChecksums.androidNdk(): String = when (Host.Current.operatingSystem) {
    OperatingSystem.Linux -> androidNdkLinux
    OperatingSystem.MacOS -> androidNdkMacos
    OperatingSystem.Windows -> androidNdkWindows
}

///////////////////////////////////////////////////////////////////////////
// Toolchain
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the Android NDK host tag.
 */
fun androidNdkHostTag(): String = when (Host.Current.operatingSystem) {
    OperatingSystem.Linux -> "linux-x86_64"
    OperatingSystem.MacOS -> "darwin-x86_64"
    OperatingSystem.Windows -> "windows-x86_64"
}