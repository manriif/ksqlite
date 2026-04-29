package modules

import SQLITE3_MC_AMALGAMATION
import komple.exec.Command
import komple.exec.CommandExecutor
import org.gradle.api.file.FileSystemOperations
import utils.cSourceFile
import utils.copyToTempDirectory
import java.io.File

///////////////////////////////////////////////////////////////////////////
// Sources
///////////////////////////////////////////////////////////////////////////

private const val EXT_WASM_PATH = "ext/wasm"
private const val GNU_MAKEFILE = "GNUmakefile"
private const val PRE_JS_CPP_JS = "api/pre-js.c-pp.js"
private const val ASSIGN_WASM_EXPORT_GLUE = "function assignWasmExports(wasmExports) {"

/**
 * Extra resources files that can be embedded in the library.
 */
fun sqliteWasmExtraResourceFileNames(sqliteName: String) = listOf(
    "$sqliteName-opfs-async-proxy.js",
    "$sqliteName-worker1.mjs",
    "$sqliteName-worker1-promiser.mjs"
)

/**
 * Performs some adjustments and fixes for WASM compilation.
 */
fun configureSqliteWasmTrunk(
    ksqliteDirectory: File,
    sqliteDirectory: File
) {
    listOf(
        GNU_MAKEFILE,
        PRE_JS_CPP_JS
    ).forEach { fileName ->
        ksqliteDirectory.resolve("$EXT_WASM_PATH/$fileName").inputStream().use { input ->
            sqliteDirectory.resolve("$EXT_WASM_PATH/$fileName").outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}

/**
 * Patches sqlite generated file [inputFile] and writes the patched content to [outputFile].
 */
fun patchGeneratedSqliteForWasm(
    sqliteName: String,
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
                        line.startsWith("  _${sqliteName}") -> {
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

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Compiles SQLite for Wasm.
 */
fun compileSqliteWasm(
    fileOperations: FileSystemOperations,
    commandExecutor: CommandExecutor,
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

    val sqliteAmalgamationSourceFile = sqliteDirectory
        .resolve(cSourceFile(SQLITE3_MC_AMALGAMATION))
        .absolutePath

    val wasmDirectory = sqliteDirectory.resolve(EXT_WASM_PATH)

    commandExecutor.execute(
        command = Command("make", "-j4", "64bit", "sqlite3.c=${sqliteAmalgamationSourceFile}"),
        workingDirectory = wasmDirectory
    )

    val generatedOutputDirectory = wasmDirectory.resolve("jswasm")

    fileOperations.copy {
        from(generatedOutputDirectory)
        into(outputDirectory)
    }
}