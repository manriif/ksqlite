@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi.interop.wasm

import kotlin.js.JsBigInt

/**
 * The [sqlite3.wasm.exports](https://sqlite.org/wasm/doc/trunk/api-wasm.md#wasm-exports-namespace)
 * namespace object is a WASM-standard part of the WASM module file and contains all "exported" C
 * functions which are built into the WASM module, as well as certain non-function values which are
 * part of the WASM module. The functions which live in this object are as low-level as it gets, in
 * terms of JS/C bindings2. They perform no automatic type conversions on their arguments or result
 * values and many, perhaps most, are cumbersome to use from JS because of that. This level of the
 * API is not generally recommended for client use but  is available for those who want to make use
 * of it. The functions in this object which are intended for client-side use are re-exported into
 * the sqlite3.capi namespace and have automatic type conversions applied to them (where applicable).
 * Some small handful of the functions get re-exported into the sqlite3.wasm namespace.
 */
internal external interface Sqlite3WasmExports {

    fun sqlite3_aggregate_context((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_auto_extension((param $var0 i64) (result i32))

    fun sqlite3_bind_blob((param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i32) (param $var4 i64) (result i32))

    fun sqlite3_bind_double((param $var0 i64) (param $var1 i32) (param $var2 f64) (result i32))

    fun sqlite3_bind_int((param $var0 i64) (param $var1 i32) (param $var2 i32) (result i32))

    fun sqlite3_bind_int64((param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32))

    fun sqlite3_bind_null((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_bind_parameter_count((param $var0 i64) (result i32))

    fun sqlite3_bind_parameter_index((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_bind_parameter_name((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_bind_pointer((param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64) (param $var4 i64) (result i32))

    fun sqlite3_bind_text((param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i32) (param $var4 i64) (result i32))

    fun sqlite3_bind_zeroblob((param $var0 i64) (param $var1 i32) (param $var2 i32) (result i32))

    fun sqlite3_busy_handler((param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32))

    fun sqlite3_busy_timeout((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_cancel_auto_extension((param $var0 i64) (result i32))

    fun sqlite3_changes((param $var0 i64) (result i32))

    fun sqlite3_changes64((param $var0 i64) (result i64))

    fun sqlite3_clear_bindings((param $var0 i64) (result i32))

    fun sqlite3_close_v2((param $var0 i64) (result i32))

    fun sqlite3_collation_needed((param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32))

    fun sqlite3_column_blob((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_column_bytes((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_column_count((param $var0 i64) (result i32))

    fun sqlite3_column_database_name((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_column_decltype((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_column_double((param $var0 i64) (param $var1 i32) (result f64))

    fun sqlite3_column_int((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_column_int64((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_column_name((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_column_origin_name((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_column_table_name((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_column_text((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_column_type((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_column_value((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_commit_hook((param $var0 i64) (param $var1 i64) (param $var2 i64) (result i64))

    fun sqlite3_compileoption_get((param $var0 i32) (result i64))

    fun sqlite3_compileoption_used((param $var0 i64) (result i32))

    fun sqlite3_complete((param $var0 i64) (result i32))

    /**
     * TODO
     */
    fun sqlite3_config()

    fun sqlite3_context_db_handle((param $var0 i64) (result i64))

    fun sqlite3_create_collation((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i64) (param $var4 i64) (result i32))

    fun sqlite3_create_collation_v2((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i64) (param $var4 i64) (param $var5 i64) (result i32))

    fun sqlite3_create_function((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i32) (param $var4 i64) (param $var5 i64) (param $var6 i64) (param $var7 i64) (result i32))

    fun sqlite3_create_function_v2(((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i32) (param $var4 i64) (param $var5 i64) (param $var6 i64) (param $var7 i64) (param $var8 i64) (result i32))

    fun sqlite3_create_module((param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (result i32))

    fun sqlite3_create_module_v2((param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (result i32))

    fun sqlite3_create_window_function((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i32) (param $var4 i64) (param $var5 i64) (param $var6 i64) (param $var7 i64) (param $var8 i64) (param $var9 i64) (result i32))

    fun sqlite3_data_count((param $var0 i64) (result i32))

    /**
     * TODO
     */
    fun sqlite3_db_config()

    fun sqlite3_db_filename((param $var0 i64) (param $var1 i64) (result i64))

    fun sqlite3_db_handle((param $var0 i64) (result i64))

    fun sqlite3_db_name((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_db_readonly((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_db_status((param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64) (param $var4 i32) (result i32))

    fun sqlite3_db_status64((param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64) (param $var4 i32) (result i32))

    fun sqlite3_declare_vtab((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_deserialize((param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i32) (result i32))

    fun sqlite3_drop_modules((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_errcode((param $var0 i64) (result i32))

    fun sqlite3_errmsg((param $var0 i64) (result i64))

    fun sqlite3_error_offset((param $var0 i64) (result i32))

    fun sqlite3_errstr((param $var0 i32) (result i64))

    fun sqlite3_exec((param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (result i32))

    fun sqlite3_expanded_sql((param $var0 i64) (result i64))

    fun sqlite3_extended_errcode((param $var0 i64) (result i32))

    fun sqlite3_extended_result_codes((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_file_control((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i64) (result i32))

    fun sqlite3_finalize((param $var0 i64) (result i32))

    fun sqlite3_free((param $var0 i64))

    fun sqlite3_get_autocommit((param $var0 i64) (result i32))

    fun sqlite3_get_auxdata((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_initialize((result i32))

    fun sqlite3_interrupt((param $var0 i64))

    fun sqlite3_is_interrupted((param $var0 i64) (result i32))

    fun sqlite3_keyword_check((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_keyword_count((result i32))

    fun sqlite3_keyword_name((param $var0 i32) (param $var1 i64) (param $var2 i64) (result i32))

    fun sqlite3_last_insert_rowid((param $var0 i64) (result i64))

    fun sqlite3_libversion((result i64))

    fun sqlite3_libversion_number((result i32))

    fun sqlite3_limit((param $var0 i64) (param $var1 i32) (param $var2 i32) (result i32))

    fun sqlite3_malloc((param $var0 i32) (result i64))

    fun sqlite3_malloc64((param $var0 i64) (result i64))

    fun sqlite3_msize((param $var0 i64) (result i64))

    fun sqlite3_next_stmt((param $var0 i64) (param $var1 i64) (result i64))

    fun sqlite3_open((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_open_v2((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i64) (result i32))

    fun sqlite3_overload_function((param $var0 i64) (param $var1 i64) (param $var2 i32) (result i32))

    fun sqlite3_prepare_v2((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i64) (param $var4 i64) (result i32))

    fun sqlite3_prepare_v3((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i32) (param $var4 i64) (param $var5 i64) (result i32))

    fun sqlite3_preupdate_blobwrite((param $var0 i64) (result i32))

    fun sqlite3_preupdate_count((param $var0 i64) (result i32))

    fun sqlite3_preupdate_depth((param $var0 i64) (result i32))

    fun sqlite3_preupdate_hook((param $var0 i64) (param $var1 i64) (param $var2 i64) (result i64))

    fun sqlite3_preupdate_new((param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32))

    fun sqlite3_preupdate_old((param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32))

    fun sqlite3_progress_handler((param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64))

    fun sqlite3_randomness((param $var0 i32) (param $var1 i64))

    fun sqlite3_realloc((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_realloc64((param $var0 i64) (param $var1 i64) (result i64))

    fun sqlite3_reset((param $var0 i64) (result i32))

    fun sqlite3_reset_auto_extension()

    fun sqlite3_result_blob((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i64))

    fun sqlite3_result_double((param $var0 i64) (param $var1 f64))

    fun sqlite3_result_error((param $var0 i64) (param $var1 i64) (param $var2 i32))

    fun sqlite3_result_error_code((param $var0 i64) (param $var1 i32))

    fun sqlite3_result_error_nomem((param $var0 i64))

    fun sqlite3_result_error_toobig((param $var0 i64))

    fun sqlite3_result_int((param $var0 i64) (param $var1 i32))

    fun sqlite3_result_int64((param $var0 i64) (param $var1 i64))

    fun sqlite3_result_null((param $var0 i64))

    fun sqlite3_result_pointer((param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64))

    fun sqlite3_result_subtype((param $var0 i64) (param $var1 i32))

    fun sqlite3_result_text((param $var0 i64) (param $var1 i64) (param $var2 i32) (param $var3 i64))

    fun sqlite3_result_zeroblob((param $var0 i64) (param $var1 i32))

    fun sqlite3_result_zeroblob64((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_rollback_hook((param $var0 i64) (param $var1 i64) (param $var2 i64) (result i64))

    fun sqlite3_serialize((param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i32) (result i64))

    fun sqlite3_set_authorizer((param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32))

    fun sqlite3_set_auxdata((param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64))

    fun sqlite3_set_errmsg((param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32))

    fun sqlite3_set_last_insert_rowid((param $var0 i64) (param $var1 i64))

    fun sqlite3_shutdown((result i32))

    fun sqlite3_sourceid((result i64))

    fun sqlite3_sql((param $var0 i64) (result i64))

    fun sqlite3_status((param $var0 i32) (param $var1 i64) (param $var2 i64) (param $var3 i32) (result i32))

    fun sqlite3_status64((param $var0 i32) (param $var1 i64) (param $var2 i64) (param $var3 i32) (result i32))

    fun sqlite3_step((param $var0 i64) (result i32))

    fun sqlite3_stmt_busy((param $var0 i64) (result i32))

    fun sqlite3_stmt_explain((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_stmt_isexplain((param $var0 i64) (result i32))

    fun sqlite3_stmt_readonly((param $var0 i64) (result i32))

    fun sqlite3_stmt_status((param $var0 i64) (param $var1 i32) (param $var2 i32) (result i32))

    fun sqlite3_strglob((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_stricmp((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_strlike((param $var0 i64) (param $var1 i64) (param $var2 i32) (result i32))

    fun sqlite3_strnicmp((param $var0 i64) (param $var1 i64) (param $var2 i32) (result i32))

    fun sqlite3_table_column_metadata((param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i64) (param $var6 i64) (param $var7 i64) (param $var8 i64) (result i32))

    fun sqlite3_total_changes((param $var0 i64) (result i32))

    fun sqlite3_total_changes64((param $var0 i64) (result i64))

    fun sqlite3_trace_v2((param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64) (result i32))

    fun sqlite3_txn_state((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_update_hook((param $var0 i64) (param $var1 i64) (param $var2 i64) (result i64))

    fun sqlite3_uri_boolean((param $var0 i64) (param $var1 i64) (param $var2 i32) (result i32))

    fun sqlite3_uri_int64((param $var0 i64) (param $var1 i64) (param $var2 i64) (result i64))

    fun sqlite3_uri_key((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_uri_parameter((param $var0 i64) (param $var1 i64) (result i64))

    fun sqlite3_user_data((param $var0 i64) (result i64))

    fun sqlite3_value_blob((param $var0 i64) (result i64))

    fun sqlite3_value_bytes((param $var0 i64) (result i32))

    fun sqlite3_value_double((param $var0 i64) (result f64))

    fun sqlite3_value_dup((param $var0 i64) (result i64))

    fun sqlite3_value_free((param $var0 i64))

    fun sqlite3_value_frombind((param $var0 i64) (result i32))

    fun sqlite3_value_int((param $var0 i64) (result i32))

    fun sqlite3_value_int64((param $var0 i64) (result i64))

    fun sqlite3_value_nochange((param $var0 i64) (result i32))

    fun sqlite3_value_numeric_type((param $var0 i64) (result i32))

    fun sqlite3_value_pointer((param $var0 i64) (param $var1 i64) (result i64))

    fun sqlite3_value_subtype((param $var0 i64) (result i32))

    fun sqlite3_value_text((param $var0 i64) (result i64))

    fun sqlite3_value_type((param $var0 i64) (result i32))

    fun sqlite3_vfs_find((param $var0 i64) (result i64))

    fun sqlite3_vfs_register((param $var0 i64) (param $var1 i32) (result i32))

    fun sqlite3_vfs_unregister((param $var0 i64) (result i32))

    fun sqlite3_vtab_collation((param $var0 i64) (param $var1 i32) (result i64))

    fun sqlite3_vtab_distinct((param $var0 i64) (result i32))

    fun sqlite3_vtab_in((param $var0 i64) (param $var1 i32) (param $var2 i32) (result i32))

    fun sqlite3_vtab_in_first((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_vtab_in_next((param $var0 i64) (param $var1 i64) (result i32))

    fun sqlite3_vtab_nochange((param $var0 i64) (result i32))

    fun sqlite3_vtab_on_conflict((param $var0 i64) (result i32))

    fun sqlite3_vtab_rhs_value((param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32))

    /**
     * (export "sqlite3_key") (param $var0 i64) (param $var1 i64) (param $var2 i32) (result i32)
     * (export "sqlite3_key_v2") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i32) (result i32)
     * (export "sqlite3_rekey") (param $var0 i64) (param $var1 i64) (param $var2 i32) (result i32)
     * (export "sqlite3_rekey_v2") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i32) (result i32)
     */

    ///////////////////////////////////////////////////////////////////////////
    // Extras
    //
    // functions extracted from the sqlite3_64bit.wasm file but not yet used
    ///////////////////////////////////////////////////////////////////////////

    /**
     * (export "sqlite3_activate_see") (param $var0 i64)
     *
     * (export "sqlite3session_diff") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (result i32)
     * (export "sqlite3session_attach") (param $var0 i64) (param $var1 i64) (result i32)
     * (export "sqlite3session_create") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3session_delete") (param $var0 i64)
     * (export "sqlite3session_table_filter") (param $var0 i64) (param $var1 i64) (param $var2 i64)
     * (export "sqlite3session_changeset") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3session_changeset_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3session_patchset_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3session_patchset") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3session_enable") (param $var0 i64) (param $var1 i32) (result i32)
     * (export "sqlite3session_indirect") (param $var0 i64) (param $var1 i32) (result i32)
     * (export "sqlite3session_isempty") (param $var0 i64) (result i32)
     * (export "sqlite3session_memory_used") (param $var0 i64) (result i64)
     * (export "sqlite3session_object_config") (param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32)
     * (export "sqlite3session_changeset_size") (param $var0 i64) (result i64)
     * (export "sqlite3session_config") (param $var0 i32) (param $var1 i64) (result i32)
     *
     * (export "sqlite3changeset_start") (param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32)
     * (export "sqlite3changeset_start_v2") (param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i32) (result i32)
     * (export "sqlite3changeset_start_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3changeset_start_v2_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i32) (result i32)
     * (export "sqlite3changeset_next") (param $var0 i64) (result i32)
     * (export "sqlite3changeset_op") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (result i32)
     * (export "sqlite3changeset_pk") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3changeset_old") (param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32)
     * (export "sqlite3changeset_new") (param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32)
     * (export "sqlite3changeset_conflict") (param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32)
     * (export "sqlite3changeset_fk_conflicts") (param $var0 i64) (param $var1 i64) (result i32)
     * (export "sqlite3changeset_finalize") (param $var0 i64) (result i32)
     * (export "sqlite3changeset_invert") (param $var0 i32) (param $var1 i64) (param $var2 i64) (param $var3 i64) (result i32)
     * (export "sqlite3changeset_apply_v2") (param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i64) (param $var6 i64) (param $var7 i64) (param $var8 i32) (result i32)
     * (export "sqlite3changeset_apply_v3") (param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i64) (param $var6 i64) (param $var7 i64) (param $var8 i32) (result i32)
     * (export "sqlite3changeset_apply") (param $var0 i64) (param $var1 i32) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i64) (result i32)
     * (export "sqlite3changeset_apply_v3_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i64) (param $var6 i64) (param $var7 i64) (param $var8 i32) (result i32)
     * (export "sqlite3changeset_apply_v2_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i64) (param $var6 i64) (param $var7 i64) (param $var8 i32) (result i32)
     * (export "sqlite3changeset_apply_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i64) (result i32)
     * (export "sqlite3changeset_concat") (param $var0 i32) (param $var1 i64) (param $var2 i32) (param $var3 i64) (param $var4 i64) (param $var5 i64) (result i32)
     * (export "sqlite3changeset_concat_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (param $var3 i64) (param $var4 i64) (param $var5 i64) (result i32)
     *
     * (export "sqlite3changegroup_new") (param $var0 i64) (result i32)
     * (export "sqlite3changegroup_add") (param $var0 i64) (param $var1 i32) (param $var2 i64) (result i32)
     * (export "sqlite3changegroup_output") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3changegroup_add_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3changegroup_output_strm") (param $var0 i64) (param $var1 i64) (param $var2 i64) (result i32)
     * (export "sqlite3changegroup_delete") (param $var0 i64)
     */
}