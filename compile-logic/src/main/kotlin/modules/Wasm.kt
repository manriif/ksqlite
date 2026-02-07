package modules

import compilation.SqliteCompilationParameters
import compilation.SqliteMcFunctions
import compilation.SqliteMcFunctionsWasmSignature
import utils.insertAfterText
import java.io.File

/**
 * Performs some adjustments and fixes for WASM compilation.
 */
fun adjustSqliteSourceTreeForWasmCompilation(
    sqliteSourcesDirectory: File,
    params: SqliteCompilationParameters
) {
    val wasmDirectory = sqliteSourcesDirectory.resolve("ext/wasm")
    configureGnuMakefile(wasmDirectory)
    patchFiles(wasmDirectory)
    configureExportedFunctions(wasmDirectory, params)
}

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

private const val MAKEFILE_SEARCH_TEXT_EMCC_FLAGS = "emcc.flags ="
private const val MAKEFILE_SEARCH_TEXT_EMCC_CFLAGS = "emcc.cflags ="

/**
 * Configures the GNUmakefile.
 */
private fun configureGnuMakefile(wasmDirectory: File) {
    // Append _WASM_ flag for sqlitemc
    check(
        wasmDirectory
            .resolve("GNUmakefile")
            .insertAfterText(
                searchTexts = listOf(
                    MAKEFILE_SEARCH_TEXT_EMCC_FLAGS,
                    MAKEFILE_SEARCH_TEXT_EMCC_CFLAGS
                ),
                contentToInsert = " -D__WASM__"
            )
    )
}

///////////////////////////////////////////////////////////////////////////
// Fixes
///////////////////////////////////////////////////////////////////////////

private const val EXTERN_POST_JS_CPP_JS = "extern-post-js.c-pp.js"
private const val PRE_JS_CPP_JS = "pre-js.c-pp.js"

/**
 * Replaces some js files.
 */
private fun patchFiles(wasmDirectory: File) {
    replaceApiFile(wasmDirectory, PRE_JS_CPP_JS)
    replaceApiFile(wasmDirectory, EXTERN_POST_JS_CPP_JS)
}

/**
 * Replaces file [fileName] in [wasmDirectory] by the one in resources.
 */
private fun replaceApiFile(wasmDirectory: File, fileName: String) {
    val resource = Thread.currentThread().contextClassLoader
        .getResourceAsStream("wasm/$fileName")
        ?: error("File resource $fileName not found in /wasm")

    resource.use { input ->
        wasmDirectory.resolve("api/$fileName").outputStream().use { output ->
            input.copyTo(output)
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

private const val GLUE_FILE_SEARCH_TEXT = """["sqlite3_activate_see", undefined, "string"]"""
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
        .resolve("api/EXPORTED_FUNCTIONS.sqlite3-see")
        .appendText(enabledSqliteMcFunctions.joinToString("") { function ->
            "_${params.sqliteMcName}_$function\n"
        })

    // Append sqlitemc functions signatures
    check(
        wasmDirectory
            .resolve("api/sqlite3-api-glue.c-pp.js")
            .insertAfterText(
                searchTexts = listOf(GLUE_FILE_SEARCH_TEXT),
                contentToInsert = enabledSqliteMcFunctions.joinToString(
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