package modules

import compilation.SqliteCompilationParameters
import compilation.SqliteMcFunctions
import compilation.SqliteMcFunctionsWasmSignature
import utils.insertAfterText
import java.io.File

/**
 * Text to search for when inserting WASM flag.
 */
private const val MAKEFILE_SEARCH_TEXT_EMCC_FLAGS = "emcc.flags ="
private const val MAKEFILE_SEARCH_TEXT_EMCC_CFLAGS = "emcc.cflags ="

/**
 * Text to search for when inserting SQLite Multiple Ciphers functions signatures.
 */
private const val GLUE_FILE_SEARCH_TEXT = """["sqlite3_activate_see", undefined, "string"]"""
private const val FUNCTION_SEPARATOR = ",\n      "

/**
 * Performs some adjustments from [SQLite Multiple Ciphers](https://utelle.github.io/SQLite3MultipleCiphers/docs/installation/install_overview/#webassembly)
 * for WASM compilation.
 */
fun adjustSqliteSourceTreeForWasmCompilation(
    sqliteSourcesDirectory: File,
    params: SqliteCompilationParameters
) {
    // Append _WASM_ flag for sqlitemc
    check(
        sqliteSourcesDirectory
            .resolve("ext/wasm/GNUmakefile")
            .insertAfterText(
                searchTexts = listOf(
                    MAKEFILE_SEARCH_TEXT_EMCC_FLAGS,
                    MAKEFILE_SEARCH_TEXT_EMCC_CFLAGS
                ),
                contentToInsert = " -D__WASM__"
            )
    )

    val enabledSqliteMcFunctions = SqliteMcFunctions.filter { it.value }.keys

    // Append the functions to export, new line at the end of the line is important
    sqliteSourcesDirectory
        .resolve("ext/wasm/api/EXPORTED_FUNCTIONS.sqlite3-see")
        .appendText(enabledSqliteMcFunctions.joinToString("") { function ->
            "_${params.sqliteMcName}_$function\n"
        })

    // Append sqlitemc functions signatures
    check(
        sqliteSourcesDirectory
            .resolve("ext/wasm/api/sqlite3-api-glue.c-pp.js")
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