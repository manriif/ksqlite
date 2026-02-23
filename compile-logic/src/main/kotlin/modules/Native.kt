package modules

import compilation.SqliteCompilationParameters
import compilation.sqliteFunctions
import platform.OperatingSystem
import java.io.File

/**
 * Definition file noStringConversions.
 */
private val NoStringConversions = listOf(
    "prepare_v2",
    "prepare_v3",
    "bind_pointer"
)

/**
 * Returns the definition file content.
 */
fun createDefContent(
    packageName: String,
    libraryFile: File,
    operatingSystem: OperatingSystem,
    params: SqliteCompilationParameters,
): String {
    val base = """
        |language = C
        |package = $packageName
        |headers = ${params.sqliteMcAmalgamationName}.h
        |headerFilter = ${params.sqliteMcAmalgamationName}.h
        |staticLibraries = ${libraryFile.name}
        |libraryPaths = ${libraryFile.parentFile.absolutePath}
        |noStringConversion = ${NoStringConversions.joinToString(" ") { "${params.sqliteName}_$it" }}
        |excludedFunctions = ${params.sqliteFunctions(false).joinToString(" ")}
    """.trimMargin()

    return when (operatingSystem) {
        is OperatingSystem.LinuxLike, OperatingSystem.MacOS -> """
            |$base
            |linkerOpts = -lpthread -ldl
        """.trimMargin()

        else -> base
    }
}