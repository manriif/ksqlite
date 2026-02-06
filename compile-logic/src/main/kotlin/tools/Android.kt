package tools

import KsqliteChecksums
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import platform.Architecture
import platform.Host
import platform.OperatingSystem
import utils.copyFirstDirectoryContent
import java.io.File

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

        // Android provides a DMG for macOS, postpone mounting at installation phase
        OperatingSystem.MacOS -> return
    }
}

/**
 * Installs the Android NDK.
 */
fun androidNdkInstall(
    version: String,
    fileOperations: FileSystemOperations,
    execOperations: ExecOperations,
    downloadedFile: File,
    inputDirectory: File,
    outputDirectory: File
) {
    when (Host.Current.operatingSystem) {
        OperatingSystem.Windows, OperatingSystem.Linux -> {
            fileOperations.copy {
                from(inputDirectory)
                into(outputDirectory)
            }
        }

        OperatingSystem.MacOS -> {
            val (major, build) = version.split('.')
            val ndkSources = "/Volumes/Android NDK $major/AndroidNDK$build.app/Contents/NDK"

            execOperations.exec {
                commandLine("hdiutil", "attach", downloadedFile.absolutePath)
            }.rethrowFailure()

            try {
                fileOperations.copy {
                    from(ndkSources)
                    into(outputDirectory)
                }
            } finally {
                execOperations.exec {
                    commandLine("hdiutil", "detach", "/Volumes/Android NDK $major")
                }.rethrowFailure()
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