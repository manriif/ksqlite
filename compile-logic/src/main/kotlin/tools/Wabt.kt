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
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Download
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the wabt download URL or `null` if it is not supported by the platform.
 */
fun wabtDownloadUrl(wabtVersion: String): String? {
    if (Host.Current.operatingSystem == OperatingSystem.Windows) {
        // Wabt has support for windows but for now logic for extraction is not implemented
        // Wabt is used for SQLite Wasm compilation but windows is not supported for now
        return null
    }

    return "https://github.com/WebAssembly/wabt/releases/download/$wabtVersion/wabt-$wabtVersion.tar.xz"
}

/**
 * Extracts wabt from [downloadedFile] into [destination].
 */
fun Task.wabtExtract(
    downloadedFile: Provider<RegularFile>,
    destination: Provider<Directory>
) {
    val execOperations = project.serviceOf<ExecOperations>()

    doFirst {
        destination.get().asFile.mkdirs()
    }

    doLast {
        execOperations.exec {
            commandLine(
                "tar",
                "-xJf",
                downloadedFile.get().asFile.absolutePath,
                "-C",
                destination.get().asFile.absolutePath,
                "--strip-components=1"
            )
        }
    }
}

/**
 * Installs and compiles Wabt.
 */
fun wabtInstall(
    fileOperations: FileSystemOperations,
    execOperations: ExecOperations,
    inputDirectory: File,
    outputDirectory: File
) {
    fileOperations.copy {
        from(inputDirectory)
        into(outputDirectory)
    }

    val buildDirectory = outputDirectory.resolve("build").apply {
        mkdirs()
    }

    execOperations.exec {
        workingDir = buildDirectory
        commandLine("bash", "-c", "cmake .. && cmake --build .")
    }
}