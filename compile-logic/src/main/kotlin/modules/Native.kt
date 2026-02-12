package modules

import compilation.SqliteCompilationParameters
import compilation.sqliteFunctions
import java.io.File

/**
 * Definition file noStringConversions.
 */
private val DefNoStringConversions = listOf(
    "prepare_v2",
    "prepare_v3"
)

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
    |noStringConversion = ${DefNoStringConversions.joinToString(" ") { "${params.sqliteName}_$it" }}
    |excludedFunctions = ${params.sqliteFunctions(false).joinToString(" ")}
""".trimMargin()