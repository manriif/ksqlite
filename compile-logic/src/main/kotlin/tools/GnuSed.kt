package tools

import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.process.ExecOperations
import platform.Host
import platform.OperatingSystem
import utils.copyFirstDirectoryContent
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Download
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the GNU sed download URL or `null` if it is not supported by the platform.
 */
fun gnuSedDownloadUrl(gnuSedVersion: String): String? {
    if (Host.Current.operatingSystem == OperatingSystem.Windows) {
        // SQLite Wasm compilation does not seem to have support for Windows
        // GNUsSed is used for SQLite Wasm compilation but is only required for macOS which make
        // wasm compilation fails if the default sed command is used
        // It should also work for Linux
        return null
    }

    return "https://ftp.gnu.org/gnu/sed/sed-$gnuSedVersion.tar.gz"
}

/**
 * Extracts GNU sed from [downloadedFile] into [destination].
 */
fun Task.gnuSedExtract(
    fileOperations: FileSystemOperations,
    downloadedFile: Provider<RegularFile>,
    destination: Provider<Directory>
) {
    val sources = project.tarTree(project.resources.gzip(downloadedFile))

    doLast {
        fileOperations.copyFirstDirectoryContent(sources, destination)
    }
}

/**
 * Installs and compiles GNU sed.
 */
fun gnuSedInstall(
    fileOperations: FileSystemOperations,
    execOperations: ExecOperations,
    inputDirectory: File,
    outputDirectory: File
) {
    fileOperations.copy {
        from(inputDirectory)
        into(outputDirectory)
    }

    val buildDirectory = outputDirectory.resolve("build").apply { mkdirs() }

    execOperations.exec {
        workingDir = outputDirectory

        commandLine(
            "bash",
            "-c",
            """
                ./configure --prefix=${buildDirectory.absolutePath} --disable-nls --without-selinux &&
                make -j4 &&
                make install
            """.trimIndent()
        )
    }

    fileOperations.copy {
        from(buildDirectory.resolve("bin"))
        into(outputDirectory.resolve("bin"))
    }
}