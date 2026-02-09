package modules

import compilation.SqliteCompilationParameters
import compilation.SqliteMcFunctions
import compilation.SqliteMcFunctionsWasmSignature
import utils.insertAfterText
import java.io.File

/**
 * Extra resources files that can be embedded in the library.
 */
fun sqliteWasmExtraResourceFileNames(sqliteName: String) = listOf(
    "$sqliteName-opfs-async-proxy.js",
    "$sqliteName-worker1.mjs",
    "$sqliteName-worker1-promiser.mjs",
    "$sqliteName-worker1-promiser-bundler-friendly.mjs"
)

/**
 * Performs some adjustments and fixes for WASM compilation.
 */
fun configureSqliteWasmTrunk(
    sqliteSourcesDirectory: File,
    params: SqliteCompilationParameters
) {
    val wasmDirectory = sqliteSourcesDirectory.resolve("ext/wasm")
    applyPatches(wasmDirectory)
    configureExportedFunctions(wasmDirectory, params)
}

///////////////////////////////////////////////////////////////////////////
// Patches
///////////////////////////////////////////////////////////////////////////

private const val GNU_MAKEFILE = "GNUmakefile"
private const val PRE_JS_CPP_JS = "api/pre-js.c-pp.js"

/**
 * Replaces some js files.
 */
private fun applyPatches(wasmDirectory: File) {
    replaceFile(wasmDirectory, GNU_MAKEFILE)
    replaceFile(wasmDirectory, PRE_JS_CPP_JS)
}

/**
 * Replaces file [fileName] in [wasmDirectory] by the one in resources.
 */
private fun replaceFile(wasmDirectory: File, fileName: String) {
    val resource = Thread.currentThread().contextClassLoader
        .getResourceAsStream("wasm/$fileName")
        ?: error("File resource $fileName not found in /wasm")

    resource.use { input ->
        wasmDirectory.resolve(fileName).outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

private const val ASSIGN_WASM_EXPORT_GLUE = "function assignWasmExports(wasmExports) {"

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
// Extensions
///////////////////////////////////////////////////////////////////////////

private const val FUNCTION_SEPARATOR = ",\n      "

/**
 * Appends exported functions and signatures.
 */
private fun configureExportedFunctions(
    wasmDirectory: File,
    params: SqliteCompilationParameters
) {
    val enabledSqliteMcFunctions = SqliteMcFunctions.filter { it.value }.keys

    // Append the functions to export, new line at the end of the line is important
    wasmDirectory
        .resolve("api/EXPORTED_FUNCTIONS.${params.sqliteName}-see")
        .appendText(enabledSqliteMcFunctions.joinToString("") { function ->
            "_${params.sqliteMcName}_$function\n"
        })

    val searchText = """["${params.sqliteName}_activate_see", undefined, "string"]"""

    // Append sqlitemc functions signatures
    check(
        wasmDirectory
            .resolve("api/${params.sqliteName}-api-glue.c-pp.js")
            .insertAfterText(
                searchText to enabledSqliteMcFunctions.joinToString(
                    separator = FUNCTION_SEPARATOR,
                    prefix = FUNCTION_SEPARATOR
                ) { function ->
                    val functionName = "${params.sqliteMcName}_$function"

                    val signatureString = checkNotNull(SqliteMcFunctionsWasmSignature[function]) {
                        "Function signature for $functionName was not found"
                    }.joinToString(", ") { """"$it"""" }

                    """["$functionName", $signatureString]"""
                })
    ) {
        "Search text for function signatures insertion was not found"
    }
}