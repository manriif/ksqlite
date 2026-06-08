///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

/**
 * Name of the SQLite product.
 * It is the name of the SQLite C header file, C source file and code source function prefix.
 */
const val SQLITE3 = "sqlite3"

/**
 * Name of the SQLite Multiple Ciphers.
 * It is the name of the code source function prefix.
 */
const val SQLITE3MC = "${SQLITE3}mc"

/**
 * Name of the SQLite Multiple Ciphers amalgamation files.
 * It is the name of the C header file, C source file.
 */
const val SQLITE3MC_AMALGAMATION = "${SQLITE3MC}_amalgamation"

///////////////////////////////////////////////////////////////////////////
// Naming
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a new list with all items prefixed with [SQLITE3] and [joint].
 */
fun Iterable<String>.sqlitePrefixed(joint: Char = '_'): List<String> {
    return map { "${SQLITE3}${joint}${it}" }
}

/**
 * Returns a new map with all keys prefixed with [SQLITE3] and [joint].
 */
fun <T> Map<String, T>.sqlitePrefixed(joint: Char = '_'): Map<String, T> {
    return mapKeys { "${SQLITE3}${joint}${it.key}" }
}

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Definitions for SQLite compilation.
 */
val SqliteDefinitions = mapOf(
    "CODEC_TYPE" to "CODEC_TYPE_CHACHA20",
    "SQLITE_ENABLE_FTS5" to "1",
    "SQLITE_ENABLE_JSON1" to "1",
    "SQLITE_ENABLE_COLUMN_METADATA" to "1",
    "SQLITE_ENABLE_MATH_FUNCTIONS" to "1",
    "SQLITE_ENABLE_NORMALIZE" to "1",
    "SQLITE_ENABLE_OFFSET_SQL_FUNC" to "1",
    "SQLITE_ENABLE_PERCENTILE" to "1",
    "SQLITE_ENABLE_PREUPDATE_HOOK" to "1",
    "SQLITE_ENABLE_RTREE" to "1",
    //"SQLITE_ENABLE_SESSION" to "1",
    "SQLITE_ENABLE_SNAPSHOT" to "1",
    "SQLITE_ENABLE_SQLLOG" to "1",
    "SQLITE_ENABLE_UNKNOWN_SQL_FUNCTION" to "1",
    "SQLITE_OMIT_AUTOINIT" to "1",
    "SQLITE_OMIT_DEPRECATED" to "1",
    "SQLITE_OMIT_LOAD_EXTENSION" to "1",
    "SQLITE_OMIT_UTF16" to "1",
    "SQLITE_OMIT_SHARED_CACHE" to "1",
    "SQLITE_TEMP_STORE" to "2",
    "SQLITE_USE_URI" to "1",
)

/**
 * Linker options for Unix based OS.
 */
val SqliteUnixLinkerOptions = listOf(
    "-lpthread",
    "-ldl"
)

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

/**
 * Structs that must be exported.
 */
val Sqlite3Structs = listOf(
    "index_info",
    "module",
    "vtab",
    "vtab_cursor"
).sqlitePrefixed()

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * SQLite 3 functions with their enabled state.
 */
private val Sqlite3Functions = mapOf(
    // Database Connection Functions
    "open" to true,
    "open16" to false,  // Use UTF-8 version instead
    "open_v2" to true,
    "close" to true,
    "close_v2" to true,

    // Statement Preparation & Execution
    "prepare" to false,  // Use v2 or v3 instead
    "prepare_v2" to true,
    "prepare_v3" to true,
    "prepare16" to false,
    "prepare16_v2" to false,
    "prepare16_v3" to false,
    "step" to true,
    "finalize" to true,
    "reset" to true,
    "exec" to true,

    // Parameter Binding
    "bind_blob" to true,
    "bind_blob64" to true,
    "bind_double" to true,
    "bind_int" to true,
    "bind_int64" to true,
    "bind_null" to true,
    "bind_text" to true,
    "bind_text16" to false,
    "bind_text64" to true,
    "bind_value" to true,
    "bind_zeroblob" to true,
    "bind_zeroblob64" to true,
    "bind_pointer" to true,
    "bind_parameter_count" to true,
    "bind_parameter_index" to true,
    "bind_parameter_name" to true,
    "clear_bindings" to true,

    // Column Access
    "column_blob" to true,
    "column_bytes" to true,
    "column_bytes16" to false,
    "column_count" to true,
    "column_double" to true,
    "column_int" to true,
    "column_int64" to true,
    "column_text" to true,
    "column_text16" to false,
    "column_type" to true,
    "column_value" to true,
    "column_name" to true,
    "column_name16" to false,
    "column_decltype" to true,
    "column_decltype16" to false,
    "column_database_name" to true,
    "column_database_name16" to false,
    "column_table_name" to true,
    "column_table_name16" to false,
    "column_origin_name" to true,
    "column_origin_name16" to false,
    "data_count" to true,

    // Error Handling
    "errcode" to true,
    "extended_errcode" to true,
    "extended_result_codes" to true,
    "errmsg" to true,
    "errmsg16" to false,
    "error_offset" to true,
    "errstr" to true,

    // Transaction & Changes
    "changes" to true,
    "changes64" to true,
    "total_changes" to true,
    "total_changes64" to true,
    "last_insert_rowid" to true,
    "set_last_insert_rowid" to true,
    "get_autocommit" to true,

    // BLOB I/O
    "blob_open" to true,
    "blob_close" to true,
    "blob_bytes" to true,
    "blob_read" to true,
    "blob_write" to true,
    "blob_reopen" to true,

    // Backup & Serialize
    "backup_init" to true,
    "backup_step" to true,
    "backup_finish" to true,
    "backup_remaining" to true,
    "backup_pagecount" to true,
    "serialize" to true,
    "deserialize" to true,

    // Custom Functions & Aggregates
    "create_function" to false,
    "create_function_v2" to true,
    "create_function16" to false,
    "create_window_function" to true,
    "aggregate_context" to true,

    // Result Functions (for custom functions)
    "result_blob" to true,
    "result_blob64" to true,
    "result_double" to true,
    "result_error" to true,
    "result_error16" to false,
    "result_error_code" to true,
    "result_error_nomem" to true,
    "result_error_toobig" to true,
    "result_int" to true,
    "result_int64" to true,
    "result_null" to true,
    "result_pointer" to true,
    "result_subtype" to true,
    "result_text" to true,
    "result_text16" to false,
    "result_text16be" to false,
    "result_text16le" to false,
    "result_text64" to true,
    "result_value" to true,
    "result_zeroblob" to true,
    "result_zeroblob64" to true,

    // Value Functions (for custom functions)
    "value_blob" to true,
    "value_bytes" to true,
    "value_bytes16" to false,
    "value_double" to true,
    "value_dup" to true,
    "value_encoding" to true,
    "value_free" to true,
    "value_frombind" to true,
    "value_int" to true,
    "value_int64" to true,
    "value_nochange" to true,
    "value_numeric_type" to true,
    "value_pointer" to true,
    "value_subtype" to true,
    "value_text" to true,
    "value_text16" to false,
    "value_text16be" to false,
    "value_text16le" to false,
    "value_type" to true,

    // Collations
    "create_collation" to false,
    "create_collation_v2" to true,
    "create_collation16" to false,
    "collation_needed" to true,
    "collation_needed16" to false,

    // Library Information
    "libversion" to true,
    "libversion_number" to true,
    "sourceid" to true,
    "threadsafe" to false,
    "version" to true,

    // Compilation Options
    "compileoption_get" to true,
    "compileoption_used" to true,

    // SQL Utilities
    "complete" to true,
    "complete16" to false,
    "sql" to true,
    "expanded_sql" to true,
    "normalized_sql" to true,

    // Configuration
    "config" to true,
    "db_config" to true,
    "initialize" to true,
    "shutdown" to true,
    "os_init" to false,
    "os_end" to false,

    // Hooks
    "commit_hook" to true,
    "rollback_hook" to true,
    "update_hook" to true,
    "trace_v2" to true,
    "progress_handler" to true,
    "busy_handler" to true,
    "busy_timeout" to true,

    // Preupdate Hook (for session extension)
    "preupdate_hook" to true,
    "preupdate_old" to true,
    "preupdate_new" to true,
    "preupdate_count" to true,
    "preupdate_depth" to true,
    "preupdate_blobwrite" to true,

    // Database Status
    "db_status" to true,
    "db_status64" to true,
    "status" to true,
    "status64" to true,
    "stmt_status" to true,

    // Statement Information
    "stmt_busy" to true,
    "stmt_readonly" to true,
    "stmt_isexplain" to true,
    "stmt_explain" to true,
    "stmt_scanstatus" to false,
    "stmt_scanstatus_v2" to false,
    "stmt_scanstatus_reset" to false,

    // Database Information
    "db_filename" to true,
    "db_handle" to true,
    "db_name" to true,
    "db_readonly" to true,
    "next_stmt" to true,

    // Memory Management
    "malloc" to true,
    "malloc64" to true,
    "realloc" to true,
    "realloc64" to true,
    "free" to true,
    "msize" to true,
    "release_memory" to true,
    "db_release_memory" to true,
    "memory_used" to true,
    "memory_highwater" to true,
    "hard_heap_limit64" to true,
    "soft_heap_limit64" to true,

    // String Functions
    "mprintf" to true,
    "vmprintf" to false,
    "snprintf" to false,
    "vsnprintf" to false,
    "str_new" to false,
    "str_finish" to false,
    "str_append" to false,
    "str_appendall" to false,
    "str_appendchar" to false,
    "str_appendf" to false,
    "str_vappendf" to false,
    "str_reset" to false,
    "str_errcode" to false,
    "str_length" to false,
    "str_value" to false,

    // String Comparison
    "strglob" to true,
    "strlike" to true,
    "stricmp" to true,
    "strnicmp" to true,

    // Mutex Functions (low-level)
    "mutex_alloc" to false,
    "mutex_free" to false,
    "mutex_enter" to false,
    "mutex_try" to false,
    "mutex_leave" to false,
    "mutex_held" to false,
    "mutex_notheld" to false,
    "db_mutex" to false,

    // Virtual File System
    "vfs_find" to true,
    "vfs_register" to true,
    "vfs_unregister" to true,

    // File Control
    "file_control" to true,
    "uri_parameter" to true,
    "uri_boolean" to true,
    "uri_int64" to true,
    "uri_key" to true,
    "filename_database" to false,
    "filename_journal" to false,
    "filename_wal" to false,
    "database_file_object" to false,
    "create_filename" to false,
    "free_filename" to false,

    // Virtual Tables
    "create_module" to false,
    "create_module_v2" to true,
    "declare_vtab" to true,
    "drop_modules" to true,
    "overload_function" to true,
    "vtab_collation" to true,
    "vtab_config" to true,
    "vtab_distinct" to true,
    "vtab_in" to true,
    "vtab_in_first" to true,
    "vtab_in_next" to true,
    "vtab_nochange" to true,
    "vtab_on_conflict" to true,
    "vtab_rhs_value" to true,

    // WAL (Write-Ahead Logging)
    "wal_hook" to true,
    "wal_autocheckpoint" to true,
    "wal_checkpoint" to true,
    "wal_checkpoint_v2" to true,

    // Snapshots
    "snapshot_get" to true,
    "snapshot_open" to true,
    "snapshot_free" to true,
    "snapshot_cmp" to true,
    "snapshot_recover" to true,

    // Extensions
    "auto_extension" to true,
    "cancel_auto_extension" to true,
    "reset_auto_extension" to true,
    "load_extension" to false,
    "enable_load_extension" to false,

    // Miscellaneous
    "interrupt" to true,
    "is_interrupted" to true,
    "limit" to true,
    "log" to true,
    "randomness" to true,
    "set_authorizer" to true,
    "sleep" to false,
    "system_errno" to true,
    "table_column_metadata" to true,
    "test_control" to false,  // For testing only
    "txn_state" to true,
    "unlock_notify" to true,
    "user_data" to true,
    "context_db_handle" to true,
    "get_auxdata" to true,
    "set_auxdata" to true,
    "get_clientdata" to false,
    "set_clientdata" to false,
    "set_errmsg" to true,
    "autovacuum_pages" to true,
    "db_cacheflush" to true,
    "keyword_count" to true,
    "keyword_name" to true,
    "keyword_check" to true,
    "carray_bind" to true,
    "enable_shared_cache" to false,  // Deprecated
    "free_table" to false,
    "get_table" to false,
    "setlk_timeout" to false,
    "win32_set_directory" to false,  // Windows-specific
    "win32_set_directory8" to false,
    "win32_set_directory16" to false,

    // Deprecated Functions (all set to false)
    "aggregate_count" to false,
    "expired" to false,
    "global_recover" to false,
    "memory_alarm" to false,
    "profile" to false,
    "soft_heap_limit" to false,
    "thread_cleanup" to false,
    "trace" to false,
    "transfer_bindings" to false,

    // SQLite3 Multiple Ciphers Extension Functions
    "key" to true,
    "key_v2" to true,
    "rekey" to true,
    "rekey_v2" to true,
    "activate_see" to false,  // Commercial SEE only
).sqlitePrefixed()

/**
 * Returns a list of SQLite functions. If [enabled] is `true` then only the functions which are
 * enabled (available in the public Ksqlite C-API) are returned, otherwise those who are excluded
 * are returned.
 */
fun sqliteFunctions(enabled: Boolean): List<String> {
    val functions = Sqlite3Functions
        .filter { it.value == enabled }
        .keys

    return if (enabled) {
        KsqliteFunctions + functions
    } else {
        functions.toList()
    }
}