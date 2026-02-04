package modules

import compilation.SqliteCompilationParameters
import compilation.SqliteFunctions
import compilation.SqliteMcFunctions
import java.io.File

/**
 * Definition file noStringConversions.
 */
private val DefNoStringConversions = listOf(
    "prepare_v2",
    "prepare_v3"
)

/**
 * Returns the concatenation of all items prefixed with [prefix] and spaced by a space.
 */
private fun Collection<String>.spaced(prefix: String): String {
    return joinToString(" ") { "${prefix}_$it" }
}

/**
 * Returns the definition file content.
 */
fun createDefContent(
    packageName: String,
    libraryFile: File,
    params: SqliteCompilationParameters,
): String = """
    |language = C
    |package = $packageName
    |headers = ${params.sqliteMcAmalgamationName}.h
    |headerFilter = ${params.sqliteMcAmalgamationName}.h
    |staticLibraries = ${libraryFile.name}
    |libraryPaths = ${libraryFile.parentFile.absolutePath}
    |linkerOpts.linux_x64 = -lpthread -ldl
    |linkerOpts.macos_x64 = -lpthread -ldl
    |noStringConversion = ${ DefNoStringConversions.spaced(params.sqliteName)}
    |excludedFunctions = ${
        SqliteFunctions.filter { !it.value }.keys.spaced(params.sqliteName) + ' ' +
                SqliteMcFunctions.filter { !it.value }.keys.spaced(params.sqliteMcName)
    }
""".trimMargin()