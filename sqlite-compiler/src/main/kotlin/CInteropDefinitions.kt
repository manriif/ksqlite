import java.io.File

/**
 * Definition file noStringConversions.
 */
private val DefNoStringConversions = listOf(
    "prepare_v2",
    "prepare_v3"
)

/**
 * Definition file excludedFunctions.
 */
private val DefExcludedFunctions = listOf(
    "mutex_held",
    "mutex_notheld",
    "column_database_name",
    "column_database_name16",
    "column_table_name",
    "column_table_name16",
    "column_origin_name",
    "column_origin_name16",
    "normalized_sql",
    "snapshot_get",
    "snapshot_free",
    "snapshot_open",
    "snapshot_cmp",
    "snapshot_recover",
    "stmt_scanstatus",
    "stmt_scanstatus_reset",
    "unlock_notify",
    "win32_set_directory",
    "win32_set_directory8",
    "win32_set_directory16"
)

/**
 * Returns the concatenation of all items prefixed with [sqlite] and spaced by a space.
 */
private fun List<String>.spacedSqliteFunctions(sqlite: String): String {
    return joinToString(" ") { "${sqlite}_$it" }
}

/**
 * Returns the definition file content.
 */
fun createDefContent(
    packageName: String,
    libraryFile: File,
    release: SqliteRelease,
): String = """
    |language = C
    |package = $packageName
    |headers = ${release.sqliteMcName}.h
    |headerFilter = ${release.sqliteMcName}.h
    |staticLibrary = ${libraryFile.name}
    |libraryPaths = ${libraryFile.parentFile.absolutePath}
    |linkerOpts.linux_x64 = -lpthread -ldl
    |linkerOpts.macos_x64 = -lpthread -ldl
    |noStringConversion = ${DefNoStringConversions.spacedSqliteFunctions(release.sqliteName)}
    |excludedFunctions = ${DefExcludedFunctions.spacedSqliteFunctions(release.sqliteName)}
""".trimMargin()