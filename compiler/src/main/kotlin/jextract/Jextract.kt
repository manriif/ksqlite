package jextract

import KsqliteChecksums
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import toolchains.Host
import utils.copyFirstDirectoryContent

///////////////////////////////////////////////////////////////////////////
// Download
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the Jextract download URL or null if Jextract is not supported on the current platform.
 */
fun jextractDownloadUrl(
    jdkVersion: String,
    jextractVersion: String
): String? {
    val platform = Host.Current.run {
        when (operatingSystem) {
            Host.OperatingSystem.Linux -> when (architecture) {
                Host.Architecture.Arm64 -> "linux-aarch64"
                Host.Architecture.X86_64 -> "linux-x64"
            }

            Host.OperatingSystem.MacOS -> when (architecture) {
                Host.Architecture.Arm64 -> "macos-aarch64"
                Host.Architecture.X86_64 -> "macos-x64"
            }

            Host.OperatingSystem.Windows -> when (architecture) {
                Host.Architecture.Arm64 -> return null
                Host.Architecture.X86_64 -> "windows-x64"
            }
        }
    }

    val major = jextractVersion.substringBefore('-')

    return "https://download.java.net/java/early_access/jextract/$jdkVersion/$major/" +
            "openjdk-$jdkVersion-jextract+${jextractVersion}_${platform}_bin.tar.gz"
}

/**
 * Extracts jextract from [downloadedFile] into [destination].
 */
fun Task.jextractExtract(
    fileOperations: FileSystemOperations,
    downloadedFile: Provider<RegularFile>,
    destination: Provider<Directory>
) {
    val sources = project.tarTree(project.resources.gzip(downloadedFile))

    doLast {
        fileOperations.copyFirstDirectoryContent(
            source = sources,
            destination = destination
        )
    }
}

/**
 * Returns the checksum for jextract and the current operating system.
 */
fun KsqliteChecksums.jextract(): String = Host.Current.run {
    when (operatingSystem) {
        Host.OperatingSystem.Linux -> when (architecture) {
            Host.Architecture.Arm64 -> jextractLinuxAarch64
            Host.Architecture.X86_64 -> jextractLinuxX64
        }

        Host.OperatingSystem.MacOS -> when (architecture) {
            Host.Architecture.Arm64 -> jextractMacosAarch64
            Host.Architecture.X86_64 -> jextractMacosX64
        }

        Host.OperatingSystem.Windows -> when (architecture) {
            Host.Architecture.Arm64 -> error("Invalid path, no build for windows-x64")
            Host.Architecture.X86_64 -> jextractWindowsX64
        }
    }
}