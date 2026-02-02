package interop

import compilation.SqliteCompilationParameters
import compilation.SqliteFunctions
import java.io.File

/**
 * Definition file noStringConversions.
 */
private val DefNoStringConversions = listOf(
    "prepare_v2",
    "prepare_v3"
)

/**
 * Returns the concatenation of all items prefixed with [sqlite] and spaced by a space.
 */
private fun Collection<String>.spacedSqliteFunctions(sqlite: String): String {
    return joinToString(" ") { "${sqlite}_$it" }
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
    |headers = ${params.sqliteMcName}.h
    |headerFilter = ${params.sqliteMcName}.h
    |staticLibraries = ${libraryFile.name}
    |libraryPaths = ${libraryFile.parentFile.absolutePath}
    |linkerOpts.linux_x64 = -lpthread -ldl
    |linkerOpts.macos_x64 = -lpthread -ldl
    |noStringConversion = ${
        DefNoStringConversions.spacedSqliteFunctions(params.sqliteName)
    }
    |excludedFunctions = ${
        SqliteFunctions.filter { !it.value }.keys.spacedSqliteFunctions(params.sqliteName)
    }
""".trimMargin()