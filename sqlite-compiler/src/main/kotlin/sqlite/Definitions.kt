package sqlite

///////////////////////////////////////////////////////////////////////////
// .Def
///////////////////////////////////////////////////////////////////////////

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
 * Returns the definition file content.
 *
 * The [sqlite] parameters correspond to the sqlite header name which is also the prefix of
 * function names.
 */
fun createDefContent(packageName: String, sqlite: String): String =  """
    |language = C
    |package = $packageName
    |headers = $sqlite.h
    |headerFilter = $sqlite.h
    |linkerOpts.linux_x64 = -lpthread -ldl
    |linkerOpts.macos_x64 = -lpthread -ldl
    |noStringConversion = ${DefNoStringConversions.joinToString(" ") { "${sqlite}_$it" }}
    |excludedFunctions = ${DefExcludedFunctions.joinToString(" ") { "${sqlite}_$it" }}
""".trimMargin()

///////////////////////////////////////////////////////////////////////////
// Task
///////////////////////////////////////////////////////////////////////////
/*
abstract val sqliteDirectory = layout.buildDirectory.dir("sqlite")
val sqliteDefDirectory = sqliteDirectory.map { it.dir("definition") }
val sqliteDownloadDirectory = sqliteDirectory.map { it.dir("download") }
val sqliteSourcesDirectory = sqliteDirectory.map { it.dir("sources") }
val sqliteArtefactsDirectory = sqliteDirectory.map { it.dir("artefacts") }
val sqliteArtefactsNativeDirectory = sqliteArtefactsDirectory.map { it.dir("native") }
val sqliteNormalizedVersion = "$major${minor.padStart(2, '0')}${patch.padStart(2, '0')}00"
val sqliteReleaseFileName = "sqlite-amalgamation-$sqliteNormalizedVersion"
val sqliteName = "sqlite$major"*/