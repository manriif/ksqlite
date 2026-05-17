package modules

import KSQLITE
import KsqliteFunctions
import SQLITE3
import SQLITE3_MC_AMALGAMATION
import cHeaderFile
import cSourceFile
import copyToTempDirectory
import komple.exec.Command
import komple.exec.CommandExecutor
import org.gradle.api.file.FileSystemOperations
import sqlitePrefixed
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Sources
///////////////////////////////////////////////////////////////////////////

private const val KSQLITE_AMALGAMATION = "${KSQLITE}_amalgamation"

private const val EXT_WASM_PATH = "ext/wasm"
private const val GNU_MAKEFILE = "${EXT_WASM_PATH}/GNUmakefile"
private const val EXT_WASM_API_PATH = "$EXT_WASM_PATH/api"
private const val PRE_JS_CPP_JS = "$EXT_WASM_API_PATH/pre-js.c-pp.js"
private const val EXPORTED_FUNCTIONS = "$EXT_WASM_API_PATH/EXPORTED_FUNCTIONS.c-pp"

private const val ASSIGN_WASM_EXPORT_GLUE = "function assignWasmExports(wasmExports) {"

/**
 * Extra resources files that can be embedded in the library.
 */
private val WasmExtraResourceFileNames = listOf(
    "opfs-async-proxy.js",
    "worker1.mjs",
    "worker1-promiser.mjs"
).sqlitePrefixed('-')

/**
 * Extra functions which aren't exported by default in the wasm build.
 * Exports with care are theses aren't tested by the official wasm team.
 * Some aren't that meaningful to use in web platforms but are exported to align at maximum with
 * other platforms.
 */
private val WasmExtraExportedFunctions = listOf(
    "autovacuum_pages",
    "backup_finish",
    "backup_init",
    "backup_pagecount",
    "backup_remaining",
    "backup_step",
    "bind_blob64",
    "bind_text64",
    "bind_value",
    "bind_zeroblob64",
    "blob_bytes",
    "blob_close",
    "blob_open",
    "blob_read",
    "blob_reopen",
    "blob_write",
    "close",
    "config",
    "db_cacheflush",
    "db_config",
    "db_release_memory",
    "log",
    "memory_used",
    "memory_highwater",
    "mprintf",
    "release_memory",
    "result_blob64",
    "result_text64",
    "result_value",
    "system_errno",
    "value_encoding",
    "vtab_config"
).sqlitePrefixed()

/**
 * Performs some adjustments and fixes for WASM compilation.
 */
fun configureSqliteWasmTrunk(
    ksqliteDirectory: File,
    sqliteDirectory: File
) {
    replaceFiles(
        sourceDirectory = ksqliteDirectory,
        destinationDirectory = sqliteDirectory,
        GNU_MAKEFILE,
        PRE_JS_CPP_JS
    )

    val exportedFunctionFile = sqliteDirectory.resolve(EXPORTED_FUNCTIONS)
    val defaultExportedFunctions = exportedFunctionFile.readText()

    sqliteDirectory.resolve(EXPORTED_FUNCTIONS).outputStream().bufferedWriter().use { output ->
        (KsqliteFunctions + WasmExtraExportedFunctions).forEach { name ->
            output.appendLine("_$name")
        }

        output.write(defaultExportedFunctions)
    }
}

/**
 * Patches sqlite generated file [inputFile] and writes the patched content to [outputFile].
 * TODO seems no longer required as of 3.53.0
 */
private fun patchGeneratedSqliteForWasm(
    inputFile: File,
    outputFile: File
) {
    outputFile.outputStream().writer().use { writer ->
        inputFile.useLines { lines ->
            val lineIterator = lines.iterator()
            var assignWasmExportsFound = false
            var inAssignWasmExports = false

            while (lineIterator.hasNext() && !assignWasmExportsFound) {
                val line = lineIterator.next()

                when {
                    inAssignWasmExports -> when {
                        line.startsWith("  _$SQLITE3") -> {
                            writer.append(' ')
                            writer.appendLine(line.substringAfter('='))
                        }

                        line == "}" -> {
                            writer.appendLine(line)
                            assignWasmExportsFound = true
                        }

                        else -> writer.appendLine(line)
                    }

                    line == ASSIGN_WASM_EXPORT_GLUE -> {
                        writer.appendLine(line)
                        inAssignWasmExports = true
                    }

                    else -> writer.appendLine(line)
                }
            }

            check(assignWasmExportsFound) {
                "assignWasmExports() function not found in ${inputFile.name}"
            }

            lineIterator.forEachRemaining(writer::appendLine)
        }
    }
}

private fun File.mergeFiles(vararg files: File) {
    val separator = System.lineSeparator().encodeToByteArray()

    outputStream().use { output ->
        files.forEach { file ->
            file.inputStream().use { input ->
                input.copyTo(output)
            }

            output.write(separator)
        }
    }
}

private fun generateKsqliteAmalgamation(
    ksqliteDirectory: File,
    sqliteDirectory: File,
    ksqliteAmalgamationHeaderFile: File,
    ksqliteAmalgamationSourceFile: File,
) {
    val sqliteMcAmalgamationHeaderFile =
        sqliteDirectory.resolve(cHeaderFile(SQLITE3_MC_AMALGAMATION))

    val sqliteMcAmalgamationSourceFile =
        sqliteDirectory.resolve(cSourceFile(SQLITE3_MC_AMALGAMATION))

    val ksqliteHeaderFile = ksqliteDirectory.resolve(cHeaderFile(KSQLITE))
    val ksqliteSourceFile = ksqliteDirectory.resolve(cSourceFile(KSQLITE))

    ksqliteAmalgamationHeaderFile.mergeFiles(
        sqliteMcAmalgamationHeaderFile,
        ksqliteHeaderFile
    )

    ksqliteAmalgamationSourceFile.mergeFiles(
        sqliteMcAmalgamationSourceFile,
        ksqliteHeaderFile,
        ksqliteSourceFile
    )
}

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Compiles SQLite for Wasm to [outputDirectory].
 */
fun compileSqliteWasm(
    fileOperations: FileSystemOperations,
    commandExecutor: CommandExecutor,
    ksqliteDirectory: File,
    sqliteDirectory: File,
    outputDirectory: File,
) {
    // A temporary directory is used to not write the original directory which will break Gradle
    // caching
    val sqliteDirectory = fileOperations.copyToTempDirectory(sqliteDirectory)

    commandExecutor.execute(
        command = Command("./configure"),
        workingDirectory = sqliteDirectory
    )

    val ksqliteAmalgamationHeaderFile = sqliteDirectory.resolve(cHeaderFile(KSQLITE_AMALGAMATION))
    val ksqliteAmalgamationSourceFile = sqliteDirectory.resolve(cSourceFile(KSQLITE_AMALGAMATION))
    val wasmDirectory = sqliteDirectory.resolve(EXT_WASM_PATH)

    generateKsqliteAmalgamation(
        ksqliteDirectory = ksqliteDirectory,
        sqliteDirectory = sqliteDirectory,
        ksqliteAmalgamationHeaderFile = ksqliteAmalgamationHeaderFile,
        ksqliteAmalgamationSourceFile = ksqliteAmalgamationSourceFile
    )

    commandExecutor.execute(
        command = Command(
            "make",
            "-j4",
            "64bit",
            "api.oo1=0",
            "sqlite3.h=${ksqliteAmalgamationHeaderFile.absolutePath}",
            "sqlite3.c=${ksqliteAmalgamationSourceFile.absolutePath}"
        ),
        workingDirectory = wasmDirectory
    )

    val generatedWasmArtifactsDirectory = wasmDirectory.resolve("jswasm")

    fileOperations.copy {
        from(generatedWasmArtifactsDirectory)
        into(outputDirectory.resolve(GENERATED_ARTIFACTS))
    }

    fileOperations.copy {
        from(ksqliteAmalgamationHeaderFile, ksqliteAmalgamationSourceFile)
        into(outputDirectory.resolve(GENERATED_SOURCES))
    }
}

/**
 * Copies the resources previously generated by [compileSqliteWasm] to [outputDirectory].
 */
fun copySqliteWasmGeneratedResources(
    fileOperations: FileSystemOperations,
    inputDirectory: File,
    outputDirectory: File,
) {
    val artifactsDirectory = inputDirectory.resolve(GENERATED_ARTIFACTS)
    val esm64Directory = artifactsDirectory.resolve("esm64")
    val sqliteFile = esm64Directory.resolve("$SQLITE3-64bit.mjs")

    patchGeneratedSqliteForWasm(
        inputFile = sqliteFile,
        outputFile = outputDirectory.resolve(sqliteFile.name)
    )

    fileOperations.copy {
        from(esm64Directory) {
            include { it.name != sqliteFile.name }
        }

        from(artifactsDirectory) {
            include { !it.isDirectory && it.name in WasmExtraResourceFileNames }
        }

        into(outputDirectory)
    }
}