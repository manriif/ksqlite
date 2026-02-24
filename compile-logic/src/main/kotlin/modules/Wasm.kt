package modules

import java.io.File

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