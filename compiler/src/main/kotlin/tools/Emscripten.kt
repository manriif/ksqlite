package tools

import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.support.serviceOf
import org.gradle.process.ExecOperations
import platform.Host
import platform.OperatingSystem
import utils.copyFirstDirectoryContent

///////////////////////////////////////////////////////////////////////////
// Download
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the emsdk download URL.
 */
fun emsdkDownloadUrl(emscriptenVersion: String): String {
    return "https://github.com/emscripten-core/emsdk/archive/refs/tags/$emscriptenVersion.zip"
}

/**
 * Extracts emsdk from [downloadedFile] into [destination].
 */
fun Task.emsdkExtract(
    fileOperations: FileSystemOperations,
    downloadedFile: Provider<RegularFile>,
    destination: Provider<Directory>
) {
    val sources = project.zipTree(downloadedFile)

    doLast {
        fileOperations.copyFirstDirectoryContent(
            source = sources,
            destination = destination
        )
    }
}

/**
 * Installs Emscripten.
 */
fun Task.emscriptenInstall(
    inputDirectory: Provider<Directory>,
    outputDirectory: Provider<Directory>
) {
    val fileOperations = project.serviceOf<FileSystemOperations>()
    val execOperations = project.serviceOf<ExecOperations>()

    doLast {
        fileOperations.copy {
            from(inputDirectory)
            into(outputDirectory)
        }

        val emsdk = when (Host.Current.operatingSystem) {
            OperatingSystem.Linux, OperatingSystem.MacOS -> "./emsdk"
            OperatingSystem.Windows -> "emsdk.bat"
        }

        fun exec(vararg args: String) = execOperations
            .exec {
                workingDir = outputDirectory.get().asFile
                commandLine(emsdk, *args)
            }
            .rethrowFailure()

        exec("install", "latest")
        exec("activate", "latest")
    }
}