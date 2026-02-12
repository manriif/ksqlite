package modules

import java.io.File

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
fun configureSqliteWasmTrunk(sqliteSourcesDirectory: File) {
    val wasmDirectory = sqliteSourcesDirectory.resolve("ext/wasm")
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