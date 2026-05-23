package ksqlite.capi

import ksqlite.capi.types.Sqlite3TextEncoding

///////////////////////////////////////////////////////////////////////////
// Keys used to replace callbacks in memory manager
///////////////////////////////////////////////////////////////////////////

internal const val KEY_AUTOVACUUM_PAGES = "autovacuum_pages"
internal const val KEY_BUSY_HANDLER = "busy_handler"
internal const val KEY_COLLATION_NEEDED = "collation_needed"
internal const val KEY_COMMIT_HOOK = "commit_hook"
internal const val KEY_CONFIG_LOG = "config_log"
internal const val KEY_CONFIG_SQLLOG = "config_sqllog"
internal const val KEY_CREATE_COLLATION = "create_collation"
internal const val KEY_PREUPDATE_HOOK = "preupdate_hook"
internal const val KEY_PROGRESS_HANDLER = "progress_handler"
internal const val KEY_ROLLBACK_HOOK = "rollback_hook"
internal const val KEY_SET_AUTHORIZER = "set_authorizer"
internal const val KEY_TRACE = "trace"
internal const val KEY_UPDATE_HOOK = "update_hook"
internal const val KEY_WAL_HOOK = "wal_hook"

/**
 * Returns a unique name for a function handler given theses distinctive arguments.
 */
internal fun appFunctionKey(
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding
): String {
    return "create_function_$name$nArg${encoding.value}"
}

/**
 * Returns a unique name for a window function handler given theses distinctive arguments.
 */
internal fun appWindowFunctionKey(
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding
): String {
    return "create_window_function_$name$nArg${encoding.value}"
}

/**
 * Returns a unique name for a module.
 */
internal fun moduleKey(name: String): String {
    return "create_module_$name"
}