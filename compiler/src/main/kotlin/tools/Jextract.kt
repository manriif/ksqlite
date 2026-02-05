package tools

import KsqliteChecksums
import compilation.SqliteCompilationParameters
import compilation.SqliteFunctions
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
 * Returns the Jextract download URL or null if Jextract is not supported on the current platform.
 */
fun jextractDownloadUrl(
    jdkVersion: String,
    jextractVersion: String
): String? {
    val platform = Host.Current.run {
        when (operatingSystem) {
            OperatingSystem.Linux -> when (architecture) {
                Architecture.Arm64 -> "linux-aarch64"
                Architecture.X64 -> "linux-x64"
            }

            OperatingSystem.MacOS -> when (architecture) {
                Architecture.Arm64 -> "macos-aarch64"
                Architecture.X64 -> "macos-x64"
            }

            OperatingSystem.Windows -> when (architecture) {
                Architecture.Arm64 -> return null
                Architecture.X64 -> "windows-x64"
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
        OperatingSystem.Linux -> when (architecture) {
            Architecture.Arm64 -> jextractLinuxAarch64
            Architecture.X64 -> jextractLinuxX64
        }

        OperatingSystem.MacOS -> when (architecture) {
            Architecture.Arm64 -> jextractMacosAarch64
            Architecture.X64 -> jextractMacosX64
        }

        OperatingSystem.Windows -> when (architecture) {
            Architecture.Arm64 -> error("Invalid path, no build for windows-x64")
            Architecture.X64 -> jextractWindowsX64
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Generation
///////////////////////////////////////////////////////////////////////////

/**
 * Generates the SQLite bindings with Jextract.
 */
fun jextractGenerateBindings(
    execOperations: ExecOperations,
    packageName: String,
    jextractDirectory: File,
    sqliteHeaderFile: File,
    outputDirectory: File,
    params: SqliteCompilationParameters
) {
    execOperations
        .exec {
            workingDir = jextractDirectory

            val sqliteIncludedFunctions = SqliteFunctions
                .filter { it.value }
                .flatMap { listOf("--include-function", "${params.sqliteName}_${it.key}") }

            val sqliteMcIncludedFunctions = SqliteFunctions
                .filter { it.value }
                .flatMap { listOf("--include-function", "${params.sqliteMcName}_${it.key}") }

            val includedFunctions = (sqliteIncludedFunctions + sqliteMcIncludedFunctions)
                .toTypedArray()

            commandLine(
                "bin/jextract",
                "--output", outputDirectory.absolutePath,
                "--target-package", packageName,
                "--header-class-name", params.sqliteName,
                "--include-dir", sqliteHeaderFile.parentFile.absolutePath,
                *includedFunctions,
                sqliteHeaderFile.absolutePath
            )
        }
        .rethrowFailure()
}
