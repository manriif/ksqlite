package ksqlite.capi.interop.api

import kotlin.js.JsAny
import kotlin.js.JsBigInt

/**
 * SQLite C-API exposed functions.
 */
@Suppress("FunctionName", "SpellCheckingInspection")
public external interface Sqlite3Capi : JsAny {

    public fun sqlite3_aggregate_context(ctx: JsBigInt, nBytes: Int): JsBigInt

    public fun sqlite3_auto_extension(xEntryPoint: JsAny)

    public fun sqlite3_bind_blob()

    public fun sqlite3_bind_double()

    public fun sqlite3_bind_int()

    public fun sqlite3_bind_int64()

    public fun sqlite3_bind_null()

    public fun sqlite3_bind_parameter_count()

    public fun sqlite3_bind_parameter_index()

    public fun sqlite3_bind_parameter_name()

    public fun sqlite3_bind_pointer()

    public fun sqlite3_bind_text()

    public fun sqlite3_busy_handler()

    public fun sqlite3_busy_timeout()

    public fun sqlite3_cancel_auto_extension()

    public fun sqlite3_changes()

    public fun sqlite3_changes64()

    public fun sqlite3_clear_bindings()

    public fun sqlite3_close_v2()

    public fun sqlite3_collation_needed()

    public fun sqlite3_column_blob()

    public fun sqlite3_column_bytes()

    public fun sqlite3_column_count()

    public fun sqlite3_column_database_name()

    public fun sqlite3_column_decltype()

    public fun sqlite3_column_double()

    public fun sqlite3_column_int()

    public fun sqlite3_column_int64()

    public fun sqlite3_column_js()

    public fun sqlite3_column_name()

    public fun sqlite3_column_origin_name()

    public fun sqlite3_column_table_name()

    public fun sqlite3_column_text()

    public fun sqlite3_column_type()

    public fun sqlite3_column_value()

    public fun sqlite3_commit_hook()

    public fun sqlite3_compileoption_get()

    public fun sqlite3_compileoption_used()

    public fun sqlite3_complete()

    public fun sqlite3_config()

    public fun sqlite3_context_db_handle()

    public fun sqlite3_create_collation()

    public fun sqlite3_create_collation_v2()

    public fun sqlite3_create_function()

    public fun sqlite3_create_function_v2()

    public fun sqlite3_create_module()

    public fun sqlite3_create_module_v2()

    public fun sqlite3_create_window_function()

    public fun sqlite3_data_count()

    public fun sqlite3_db_config()

    public fun sqlite3_db_filename()

    public fun sqlite3_db_handle()

    public fun sqlite3_db_name()

    public fun sqlite3_db_readonly()

    public fun sqlite3_db_status()

    public fun sqlite3_db_status64()

    public fun sqlite3_declare_vtab()

    public fun sqlite3_deserialize()

    public fun sqlite3_drop_modules()

    public fun sqlite3_errcode()

    public fun sqlite3_errmsg()

    public fun sqlite3_error_offset()

    public fun sqlite3_errstr()

    public fun sqlite3_exec()

    public fun sqlite3_expanded_sql()

    public fun sqlite3_extended_errcode()

    public fun sqlite3_extended_result_codes()

    public fun sqlite3_file_control()

    public fun sqlite3_finalize()

    public fun sqlite3_free()

    public fun sqlite3_get_autocommit()

    public fun sqlite3_get_auxdata()

    public fun sqlite3_initialize()

    public fun sqlite3_interrupt()

    public fun sqlite3_is_interrupted()

    public fun sqlite3_js_aggregate_context()

    public fun sqlite3_js_db_export()

    public fun sqlite3_js_db_uses_vfs()

    public fun sqlite3_js_db_vfs()

    public fun sqlite3_js_kvvfs_clear()

    public fun sqlite3_js_kvvfs_size()

    public fun sqlite3_js_posix_create_file()

    public fun sqlite3_js_rc_str()

    public fun sqlite3_js_sql_to_string()

    public fun sqlite3_js_vfs_create_file()

    public fun sqlite3_js_vfs_list()

    public fun sqlite3_keyword_check()

    public fun sqlite3_keyword_count()

    public fun sqlite3_keyword_name()

    public fun sqlite3_last_insert_rowid()

    public fun sqlite3_libversion(): String

    public fun sqlite3_libversion_number(): Int

    public fun sqlite3_limit()

    public fun sqlite3_malloc()

    public fun sqlite3_malloc64()

    public fun sqlite3_msize()

    public fun sqlite3_next_stmt()

    public fun sqlite3_open()

    public fun sqlite3_open_v2()

    public fun sqlite3_overload_function()

    public fun sqlite3_prepare_v2()

    public fun sqlite3_prepare_v3()

    public fun sqlite3_preupdate_blobwrite()

    public fun sqlite3_preupdate_count()

    public fun sqlite3_preupdate_depth()

    public fun sqlite3_preupdate_hook()

    public fun sqlite3_preupdate_new()

    public fun sqlite3_preupdate_new_js()

    public fun sqlite3_preupdate_old()

    public fun sqlite3_preupdate_old_js()

    public fun sqlite3_progress_handler()

    public fun sqlite3_randomness()

    public fun sqlite3_realloc()

    public fun sqlite3_realloc64()

    public fun sqlite3_reset()

    public fun sqlite3_reset_auto_extension()

    public fun sqlite3_result_blob()

    public fun sqlite3_result_double()

    public fun sqlite3_result_error()

    public fun sqlite3_result_error_code()

    public fun sqlite3_result_error_js()

    public fun sqlite3_result_error_nomem()

    public fun sqlite3_result_error_toobig()

    public fun sqlite3_result_int()

    public fun sqlite3_result_int64()

    public fun sqlite3_result_js()

    public fun sqlite3_result_null()

    public fun sqlite3_result_pointer()

    public fun sqlite3_result_subtype()

    public fun sqlite3_result_text()

    public fun sqlite3_result_zeroblob()

    public fun sqlite3_result_zeroblob64()

    public fun sqlite3_rollback_hook()

    public fun sqlite3_serialize()

    public fun sqlite3_set_authorizer()

    public fun sqlite3_set_auxdata()

    public fun sqlite3_set_errmsg()

    public fun sqlite3_set_last_insert_rowid()

    public fun sqlite3_shutdown()

    public fun sqlite3_sourceid()

    public fun sqlite3_sql()

    public fun sqlite3_status()

    public fun sqlite3_status64()

    public fun sqlite3_step()

    public fun sqlite3_stmt_busy()

    public fun sqlite3_stmt_explain()

    public fun sqlite3_stmt_isexplain()

    public fun sqlite3_stmt_readonly()

    public fun sqlite3_stmt_status()

    public fun sqlite3_strglob()

    public fun sqlite3_stricmp()

    public fun sqlite3_strlike()

    public fun sqlite3_strnicmp()

    public fun sqlite3_table_column_metadata()

    public fun sqlite3_total_changes()

    public fun sqlite3_total_changes64()

    public fun sqlite3_trace_v2()

    public fun sqlite3_txn_state()

    public fun sqlite3_update_hook()

    public fun sqlite3_uri_boolean()

    public fun sqlite3_uri_int64()

    public fun sqlite3_uri_key()

    public fun sqlite3_uri_parameter()

    public fun sqlite3_user_data()

    public fun sqlite3_value_blob()

    public fun sqlite3_value_bytes()

    public fun sqlite3_value_double()

    public fun sqlite3_value_dup()

    public fun sqlite3_value_free()

    public fun sqlite3_value_frombind()

    public fun sqlite3_value_int()

    public fun sqlite3_value_int64()

    public fun sqlite3_value_nochange()

    public fun sqlite3_value_numeric_type()

    public fun sqlite3_value_pointer()

    public fun sqlite3_value_subtype()

    public fun sqlite3_value_text()

    public fun sqlite3_value_to_js()

    public fun sqlite3_value_type()

    public fun sqlite3_values_to_js()

    public fun sqlite3_vfs_find()

    public fun sqlite3_vfs_register()

    public fun sqlite3_vfs_unregister()

    public fun sqlite3_vtab_collation()

    public fun sqlite3_vtab_config()

    public fun sqlite3_vtab_distinct()

    public fun sqlite3_vtab_in()

    public fun sqlite3_vtab_in_first()

    public fun sqlite3_vtab_in_next()

    public fun sqlite3_vtab_nochange()

    public fun sqlite3_vtab_on_conflict()

    public fun sqlite3_vtab_rhs_value()
}