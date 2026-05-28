@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi.interop

import kotlin.js.JsAny
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
internal external interface Sqlite3WasmExports : JsAny {

    fun ksqlite_auto_extension(
        p0: JsBigInt,
    ): Int

    fun ksqlite_cancel_auto_extension(
        p0: JsBigInt,
    ): Int

    fun sqlite3_aggregate_context(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_auto_extension(
        p0: JsBigInt,
    ): Int

    fun sqlite3_autovacuum_pages(
		p0: JsBigInt, 
		p1: JsBigInt, 
		p2: JsBigInt, 
		p3: JsBigInt, 
	): Int

    fun sqlite3_backup_finish(
		p0: JsBigInt, 
	): Int

    fun sqlite3_backup_init(
		p0: JsBigInt, 
		p1: JsBigInt, 
		p2: JsBigInt, 
		p3: JsBigInt, 
	): JsBigInt

    fun sqlite3_backup_pagecount(
		p0: JsBigInt, 
	): Int

    fun sqlite3_backup_remaining(
		p0: JsBigInt, 
	): Int

    fun sqlite3_backup_step(
		p0: JsBigInt, 
		p1: Int, 
	): Int

    fun sqlite3_bind_blob(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
        p3: Int,
        p4: JsBigInt,
    ): Int

    fun sqlite3_bind_blob64(
		p0: JsBigInt, 
		p1: Int, 
		p2: JsBigInt, 
		p3: JsBigInt, 
		p4: JsBigInt, 
	): Int

    fun sqlite3_bind_double(
        p0: JsBigInt,
        p1: Int,
        p2: Double,
    ): Int

    fun sqlite3_bind_int(
        p0: JsBigInt,
        p1: Int,
        p2: Int,
    ): Int

    fun sqlite3_bind_int64(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
    ): Int

    fun sqlite3_bind_null(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_bind_parameter_count(
        p0: JsBigInt,
    ): Int

    fun sqlite3_bind_parameter_index(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_bind_parameter_name(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_bind_pointer(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
        p3: JsBigInt,
        p4: JsBigInt,
    ): Int

    fun sqlite3_bind_text(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
        p3: Int,
        p4: JsBigInt,
    ): Int

    fun sqlite3_bind_text64(
		p0: JsBigInt, 
		p1: Int, 
		p2: JsBigInt, 
		p3: JsBigInt, 
		p4: JsBigInt, 
		p5: Int, 
	): Int

    fun sqlite3_bind_value(
		p0: JsBigInt, 
		p1: Int, 
		p2: JsBigInt, 
	): Int

    fun sqlite3_bind_zeroblob(
        p0: JsBigInt,
        p1: Int,
        p2: Int,
    ): Int

    fun sqlite3_bind_zeroblob64(
		p0: JsBigInt, 
		p1: Int, 
		p2: JsBigInt, 
	): Int

    fun sqlite3_blob_bytes(
		p0: JsBigInt, 
	): Int

    fun sqlite3_blob_close(
		p0: JsBigInt, 
	): Int

    fun sqlite3_blob_open(
		p0: JsBigInt, 
		p1: JsBigInt, 
		p2: JsBigInt, 
		p3: JsBigInt, 
		p4: JsBigInt, 
		p5: Int, 
		p6: JsBigInt, 
	): Int

    fun sqlite3_blob_read(
		p0: JsBigInt, 
		p1: JsBigInt, 
		p2: Int, 
		p3: Int, 
	): Int

    fun sqlite3_blob_reopen(
		p0: JsBigInt, 
		p1: JsBigInt, 
	): Int

    fun sqlite3_blob_write(
		p0: JsBigInt, 
		p1: JsBigInt, 
		p2: Int, 
		p3: Int, 
	): Int

    fun sqlite3_busy_handler(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
    ): Int

    fun sqlite3_busy_timeout(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_cancel_auto_extension(
        p0: JsBigInt,
    ): Int

    fun sqlite3_changes(
        p0: JsBigInt,
    ): Int

    fun sqlite3_changes64(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_clear_bindings(
        p0: JsBigInt,
    ): Int

    fun sqlite3_close(
		p0: JsBigInt, 
	): Int

    fun sqlite3_close_v2(
        p0: JsBigInt,
    ): Int

    fun sqlite3_collation_needed(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
    ): Int

    fun sqlite3_column_blob(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_column_bytes(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_column_count(
        p0: JsBigInt,
    ): Int

    fun sqlite3_column_database_name(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_column_decltype(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_column_double(
        p0: JsBigInt,
        p1: Int,
    ): Double

    fun sqlite3_column_int(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_column_int64(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_column_name(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_column_origin_name(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_column_table_name(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_column_text(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_column_type(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_column_value(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_commit_hook(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
    ): JsBigInt

    fun sqlite3_compileoption_get(
        p0: Int,
    ): JsBigInt

    fun sqlite3_compileoption_used(
        p0: JsBigInt,
    ): Int

    fun sqlite3_complete(
        p0: JsBigInt,
    ): Int

    fun sqlite3_config(
		p0: Int, 
		p1: JsBigInt, 
	): Int

    fun sqlite3_context_db_handle(
        p0: JsBigInt,
    ): JsBigInt

    /*fun sqlite3_create_collation(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: JsBigInt,
        p4: JsBigInt,
    ): Int*/

    fun sqlite3_create_collation_v2(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: JsBigInt,
        p4: JsBigInt,
        p5: JsBigInt,
    ): Int

    /*fun sqlite3_create_function(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: Int,
        p4: JsBigInt,
        p5: JsBigInt,
        p6: JsBigInt,
        p7: JsBigInt,
    ): Int*/

    fun sqlite3_create_function_v2(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: Int,
        p4: JsBigInt,
        p5: JsBigInt,
        p6: JsBigInt,
        p7: JsBigInt,
        p8: JsBigInt,
    ): Int

    /*fun sqlite3_create_module(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: JsBigInt,
    ): Int

    fun sqlite3_create_module_v2(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: JsBigInt,
        p4: JsBigInt,
    ): Int*/

    fun sqlite3_create_window_function(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: Int,
        p4: JsBigInt,
        p5: JsBigInt,
        p6: JsBigInt,
        p7: JsBigInt,
        p8: JsBigInt,
        p9: JsBigInt,
    ): Int

    fun sqlite3_data_count(
        p0: JsBigInt,
    ): Int

    fun sqlite3_db_cacheflush(
		p0: JsBigInt, 
	): Int

    fun sqlite3_db_config(
		p0: JsBigInt, 
		p1: Int, 
		p2: JsBigInt, 
	): Int

    fun sqlite3_db_filename(
        p0: JsBigInt,
        p1: JsBigInt,
    ): JsBigInt

    fun sqlite3_db_handle(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_db_name(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_db_readonly(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_db_release_memory(
		p0: JsBigInt, 
	): Int

    fun sqlite3_db_status(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
        p3: JsBigInt,
        p4: Int,
    ): Int

    fun sqlite3_db_status64(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
        p3: JsBigInt,
        p4: Int,
    ): Int

    fun sqlite3_declare_vtab(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_deserialize(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: JsBigInt,
        p4: JsBigInt,
        p5: Int,
    ): Int

    fun sqlite3_drop_modules(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_errcode(
        p0: JsBigInt,
    ): Int

    fun sqlite3_errmsg(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_error_offset(
        p0: JsBigInt,
    ): Int

    fun sqlite3_errstr(
        p0: Int,
    ): JsBigInt

    fun sqlite3_exec(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: JsBigInt,
        p4: JsBigInt,
    ): Int

    fun sqlite3_expanded_sql(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_extended_errcode(
        p0: JsBigInt,
    ): Int

    fun sqlite3_extended_result_codes(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_file_control(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: JsBigInt,
    ): Int

    fun sqlite3_finalize(
        p0: JsBigInt,
    ): Int

    fun sqlite3_free(
        p0: JsBigInt,
    )

    fun sqlite3_get_autocommit(
        p0: JsBigInt,
    ): Int

    fun sqlite3_get_auxdata(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_initialize(): Int

    fun sqlite3_interrupt(
        p0: JsBigInt,
    )

    fun sqlite3_is_interrupted(
        p0: JsBigInt,
    ): Int

    fun sqlite3_key(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
    ): Int

    fun sqlite3_key_v2(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: Int,
    ): Int

    fun sqlite3_keyword_check(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_keyword_count(): Int

    fun sqlite3_keyword_name(
        p0: Int,
        p1: JsBigInt,
        p2: JsBigInt,
    ): Int

    fun sqlite3_last_insert_rowid(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_libversion(): JsBigInt

    fun sqlite3_libversion_number(): Int

    fun sqlite3_limit(
        p0: JsBigInt,
        p1: Int,
        p2: Int,
    ): Int

    fun sqlite3_log(
		p0: Int, 
		p1: JsBigInt, 
		p2: JsBigInt
    )

    fun sqlite3_malloc(
        p0: Int,
    ): JsBigInt

    fun sqlite3_malloc64(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_mprintf(
        p0: JsBigInt,
        p1: JsBigInt,
    ): JsBigInt

    fun sqlite3_memory_used(
	): JsBigInt

    fun sqlite3_memory_highwater(
		p0: Int, 
	): JsBigInt

    fun sqlite3_msize(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_next_stmt(
        p0: JsBigInt,
        p1: JsBigInt,
    ): JsBigInt

    fun sqlite3_open(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_open_v2(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: JsBigInt,
    ): Int

    fun sqlite3_overload_function(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
    ): Int

    fun sqlite3_prepare_v2(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: JsBigInt,
        p4: JsBigInt,
    ): Int

    fun sqlite3_prepare_v3(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: Int,
        p4: JsBigInt,
        p5: JsBigInt,
    ): Int

    fun sqlite3_preupdate_blobwrite(
        p0: JsBigInt,
    ): Int

    fun sqlite3_preupdate_count(
        p0: JsBigInt,
    ): Int

    fun sqlite3_preupdate_depth(
        p0: JsBigInt,
    ): Int

    fun sqlite3_preupdate_hook(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
    ): JsBigInt

    fun sqlite3_preupdate_new(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
    ): Int

    fun sqlite3_preupdate_old(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
    ): Int

    fun sqlite3_progress_handler(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
        p3: JsBigInt,
    )

    fun sqlite3_randomness(
        p0: Int,
        p1: JsBigInt,
    )

    fun sqlite3_realloc(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_realloc64(
        p0: JsBigInt,
        p1: JsBigInt,
    ): JsBigInt

    fun sqlite3_rekey(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
    ): Int

    fun sqlite3_rekey_v2(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: Int,
    ): Int

    fun sqlite3_release_memory(
		p0: Int, 
	): Int

    fun sqlite3_reset(
        p0: JsBigInt,
    ): Int

    fun sqlite3_reset_auto_extension()

    fun sqlite3_result_blob(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: JsBigInt,
    )

    fun sqlite3_result_blob64(
		p0: JsBigInt, 
		p1: JsBigInt, 
		p2: JsBigInt, 
		p3: JsBigInt,)

    fun sqlite3_result_double(
        p0: JsBigInt,
        p1: Double,
    )

    fun sqlite3_result_error(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
    )

    fun sqlite3_result_error_code(
        p0: JsBigInt,
        p1: Int,
    )

    fun sqlite3_result_error_nomem(
        p0: JsBigInt,
    )

    fun sqlite3_result_error_toobig(
        p0: JsBigInt,
    )

    fun sqlite3_result_int(
        p0: JsBigInt,
        p1: Int,
    )

    fun sqlite3_result_int64(
        p0: JsBigInt,
        p1: JsBigInt,
    )

    fun sqlite3_result_null(
        p0: JsBigInt,
    )

    fun sqlite3_result_pointer(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: JsBigInt,
    )

    fun sqlite3_result_subtype(
        p0: JsBigInt,
        p1: Int,
    )

    fun sqlite3_result_text(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
        p3: JsBigInt,
    )

    fun sqlite3_result_text64(
		p0: JsBigInt, 
		p1: JsBigInt, 
		p2: JsBigInt, 
		p3: JsBigInt, 
		p4: Int,)

    fun sqlite3_result_zeroblob(
        p0: JsBigInt,
        p1: Int,
    )

    fun sqlite3_result_zeroblob64(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_result_value(
		p0: JsBigInt, 
		p1: JsBigInt,)

    fun sqlite3_rollback_hook(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
    ): JsBigInt

    fun sqlite3_serialize(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: Int,
    ): JsBigInt

    fun sqlite3_set_authorizer(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
    ): Int

    fun sqlite3_set_auxdata(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
        p3: JsBigInt,
    )

    fun sqlite3_set_errmsg(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
    ): Int

    fun sqlite3_set_last_insert_rowid(
        p0: JsBigInt,
        p1: JsBigInt,
    )

    fun sqlite3_shutdown(): Int

    fun sqlite3_sourceid(): JsBigInt

    fun sqlite3_sql(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_status(
        p0: Int,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: Int,
    ): Int

    fun sqlite3_status64(
        p0: Int,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: Int,
    ): Int

    fun sqlite3_step(
        p0: JsBigInt,
    ): Int

    fun sqlite3_stmt_busy(
        p0: JsBigInt,
    ): Int

    fun sqlite3_stmt_explain(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_stmt_isexplain(
        p0: JsBigInt,
    ): Int

    fun sqlite3_stmt_readonly(
        p0: JsBigInt,
    ): Int

    fun sqlite3_stmt_status(
        p0: JsBigInt,
        p1: Int,
        p2: Int,
    ): Int

    fun sqlite3_strglob(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_stricmp(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_strlike(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
    ): Int

    fun sqlite3_strnicmp(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
    ): Int

    fun sqlite3_system_errno(
		p0: JsBigInt, 
	): Int

    fun sqlite3_table_column_metadata(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
        p3: JsBigInt,
        p4: JsBigInt,
        p5: JsBigInt,
        p6: JsBigInt,
        p7: JsBigInt,
        p8: JsBigInt,
    ): Int

    fun sqlite3_total_changes(
        p0: JsBigInt,
    ): Int

    fun sqlite3_total_changes64(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_trace_v2(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
        p3: JsBigInt,
    ): Int

    fun sqlite3_txn_state(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_update_hook(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
    ): JsBigInt

    fun sqlite3_uri_boolean(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: Int,
    ): Int

    fun sqlite3_uri_int64(
        p0: JsBigInt,
        p1: JsBigInt,
        p2: JsBigInt,
    ): JsBigInt

    fun sqlite3_uri_key(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_uri_parameter(
        p0: JsBigInt,
        p1: JsBigInt,
    ): JsBigInt

    fun sqlite3_user_data(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_value_blob(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_value_bytes(
        p0: JsBigInt,
    ): Int

    fun sqlite3_value_double(
        p0: JsBigInt,
    ): Double

    fun sqlite3_value_dup(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_value_encoding(
		p0: JsBigInt, 
	): Int

    fun sqlite3_value_free(
        p0: JsBigInt,
    )

    fun sqlite3_value_frombind(
        p0: JsBigInt,
    ): Int

    fun sqlite3_value_int(
        p0: JsBigInt,
    ): Int

    fun sqlite3_value_int64(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_value_nochange(
        p0: JsBigInt,
    ): Int

    fun sqlite3_value_numeric_type(
        p0: JsBigInt,
    ): Int

    fun sqlite3_value_pointer(
        p0: JsBigInt,
        p1: JsBigInt,
    ): JsBigInt

    fun sqlite3_value_subtype(
        p0: JsBigInt,
    ): Int

    fun sqlite3_value_text(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_value_type(
        p0: JsBigInt,
    ): Int

    fun sqlite3_vfs_find(
        p0: JsBigInt,
    ): JsBigInt

    fun sqlite3_vfs_register(
        p0: JsBigInt,
        p1: Int,
    ): Int

    fun sqlite3_vfs_unregister(
        p0: JsBigInt,
    ): Int

    fun sqlite3_vtab_collation(
        p0: JsBigInt,
        p1: Int,
    ): JsBigInt

    fun sqlite3_vtab_config(
		p0: JsBigInt, 
		p1: Int, 
		p2: JsBigInt, 
	): Int

    fun sqlite3_vtab_distinct(
        p0: JsBigInt,
    ): Int

    fun sqlite3_vtab_in(
        p0: JsBigInt,
        p1: Int,
        p2: Int,
    ): Int

    fun sqlite3_vtab_in_first(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_vtab_in_next(
        p0: JsBigInt,
        p1: JsBigInt,
    ): Int

    fun sqlite3_vtab_nochange(
        p0: JsBigInt,
    ): Int

    fun sqlite3_vtab_on_conflict(
        p0: JsBigInt,
    ): Int

    fun sqlite3_vtab_rhs_value(
        p0: JsBigInt,
        p1: Int,
        p2: JsBigInt,
    ): Int

    ///////////////////////////////////////////////////////////////////////////
    // Extras
    //
    // Functions extracted from the sqlite3_64bit.wasm file but not yet used.
    //
    // Usefull regexes to transform from webassembly to kotlin (to be applied in the same order):
    // 
    // \(export "(.*)"\) (.*)               -> fun $1($2)
    // \(param \$var(\d) ((i|f)(32|64))\)   -> \n\t\tp$1: $2,
    // \(result ((i|f)(32|64))\)\)          -> \n\t): $1
    // i32                                  -> Int
    // i64                                  -> JsBigInt
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