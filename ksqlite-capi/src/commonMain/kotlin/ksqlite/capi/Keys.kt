package ksqlite.capi

import ksqlite.types.SqliteFunctionTextEncoding
import ksqlite.types.SqliteTextEncoding

///////////////////////////////////////////////////////////////////////////
// Keys used to store buffers that are not copied by SQLite and are then managed by the application
///////////////////////////////////////////////////////////////////////////

internal const val KEY_DB_CONFIG_MAINDBNAME = "db_config_maindbname"

///////////////////////////////////////////////////////////////////////////
// Keys used to replace callbacks in memory manager
///////////////////////////////////////////////////////////////////////////

internal const val KEY_AUTOVACUUM_PAGES = "autovacuum_pages"
internal const val KEY_BUSY_HANDLER = "busy_handler"
internal const val KEY_COLLATION_NEEDED = "collation_needed"
internal const val KEY_COMMIT_HOOK = "commit_hook"
internal const val KEY_CONFIG_LOG = "config_log"
internal const val KEY_CONFIG_SQLLOG = "config_sqllog"
internal const val KEY_PREUPDATE_HOOK = "preupdate_hook"
internal const val KEY_PROGRESS_HANDLER = "progress_handler"
internal const val KEY_ROLLBACK_HOOK = "rollback_hook"
internal const val KEY_SET_AUTHORIZER = "set_authorizer"
internal const val KEY_TRACE = "trace"
internal const val KEY_UPDATE_HOOK = "update_hook"
internal const val KEY_WAL_HOOK = "wal_hook"

/**
 * Returns a unique name for a function handler given its identifying arguments.
 */
internal fun functionKey(
    name: String,
    nArg: Int,
    encoding: SqliteFunctionTextEncoding?
): String {
    return "create_function_${name}_${nArg}_${encoding?.value}"
}

/**
 * Returns a unique name for a window function handler given its identifying arguments.
 */
internal fun windowFunctionKey(
    name: String,
    nArg: Int,
    encoding: SqliteFunctionTextEncoding
): String {
    return "create_window_function_${name}_${nArg}_${encoding.value}"
}

/**
 * Returns a unique name for a collation given its identifying arguments.
 */
internal fun collationKey(
    name: String,
    encoding: SqliteTextEncoding,
): String {
    return "create_collation_${name}_${encoding.value}"
}

/**
 * Returns a unique name for a module.
 */
internal fun moduleKey(name: String): String {
    return "create_module_$name"
}
