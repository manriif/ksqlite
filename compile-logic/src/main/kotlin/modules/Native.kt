package modules

import compilation.SqliteCompilationParameters
import compilation.SqliteDefines
import compilation.sqliteFunctions
import platform.OperatingSystem
import java.io.File

/**
 * Definition file noStringConversions.
 */
private val NoStringConversions = listOf(
    "bind_pointer",
    "bind_text",
    "bind_text64",
    "blob_open",
    "exec",
    "keyword_check",
    "open",
    "open_v2",
    "prepare_v2",
    "prepare_v3",
    "result_error",
    "result_pointer",
    "result_text",
    "result_text64",
    "serialize",
    "table_column_metadata",
    "wal_checkpoint_v2"
)

/**
 * Returns the definition file content.
 */
fun createDefContent(
    packageName: String,
    libraryFile: File,
    headerFile: File,
    sqliteMcHeaderFile: File,
    operatingSystem: OperatingSystem,
    params: SqliteCompilationParameters,
): String {
    val base = """
        |language = C
        |package = $packageName
        |headers = ${headerFile.name}
        |headerFilter = ${headerFile.name} ${sqliteMcHeaderFile.absolutePath}
        |compilerOpts = ${SqliteDefines.joinToString(" ") { "-D$it" }}
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