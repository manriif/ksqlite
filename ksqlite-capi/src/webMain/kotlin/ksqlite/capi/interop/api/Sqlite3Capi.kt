package ksqlite.capi.interop.api

import kotlin.js.JsAny
import kotlin.js.JsBigInt

/**
 * SQLite C-API exposed functions.
 */
@Suppress("FunctionName", "SpellCheckingInspection")
internal external interface Sqlite3Capi : JsAny {

    fun sqlite3_preupdate_new_js()

    fun sqlite3_preupdate_old_js()

    fun sqlite3_js_aggregate_context()

    fun sqlite3_js_db_export()

    fun sqlite3_js_db_uses_vfs()

    fun sqlite3_js_db_vfs()

    fun sqlite3_js_kvvfs_clear()

    fun sqlite3_js_kvvfs_size()

    fun sqlite3_js_posix_create_file()

    fun sqlite3_js_rc_str()

    fun sqlite3_js_sql_to_string()

    fun sqlite3_js_vfs_create_file()

    fun sqlite3_js_vfs_list()

    fun sqlite3_value_to_js()

    fun sqlite3_values_to_js()

    fun sqlite3_vtab_config()

    fun sqlite3_result_error_js()

    fun sqlite3_result_js()

    fun sqlite3_column_js()
}