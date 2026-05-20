@file:JvmName("KsqliteJni")
@file:Suppress("FunctionName")

package ksqlite

import java.nio.ByteBuffer

///////////////////////////////////////////////////////////////////////////
// Library
///////////////////////////////////////////////////////////////////////////

/**
 * Loads the Kotlin SQLite library (including SQLite Official JNI layer and SQLite).
 */
public fun ksqliteLoadLibrary() {
    System.loadLibrary(KSQLITE_NATIVE_LIB_NAME)
}

///////////////////////////////////////////////////////////////////////////
// Buffer helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a [ByteBuffer] wrapping [size] bytes from the memory region at the address [pointer].
 * Returns `null` if [pointer] points to `nullptr`.
 */
public external fun createByteBuffer(
    pointer: Long,
    size: Long
): ByteBuffer?

/**
 * Returns a [ByteBuffer] wrapping [size] bytes from the memory region at the address [pointer].
 */
public fun requireBuffer(
    pointer: Long,
    size: Long
): ByteBuffer = checkNotNull(createByteBuffer(pointer, size)) {
    "Cannot create a ByteBuffer from a null pointer"
}

public external fun nativeBufferRead(
    buffer: ByteBuffer,
    size: Int,
    sourceOffset: Long,
    destinationOffset: Int,
    destination: ByteArray
)

public external fun nativeBufferWrite(
    buffer: ByteBuffer,
    source: ByteArray,
    size: Int,
    sourceOffset: Int,
    destinationOffset: Long
)

///////////////////////////////////////////////////////////////////////////
// C-API
///////////////////////////////////////////////////////////////////////////

/**
 * To align with other platforms behavior, only one auto extension is allowed.
 */
public external fun ksqlite_auto_extension(callback: AutoExtensionCallback): Int

public external fun ksqlite_cancel_auto_extension(callback: AutoExtensionCallback): Int

public external fun sqlite3_aggregate_context(
    context: Long,
    nBytes: Int,
): Long

/**
 * Replaces the previous callback if any.
 */
public external fun sqlite3_autovacuum_pages(
    db: Long,
    callback: AutoVacuumPagesCallback?,
    destructor: DestructorCallback?,
): Int


public external fun sqlite3_backup_finish(backup: Long): Int

public external fun sqlite3_backup_init(
    destDb: Long,
    destDbName: String,
    srcDb: Long,
    srcDbName: String
): Long

public external fun sqlite3_backup_pagecount(backup: Long): Int

public external fun sqlite3_backup_remaining(backup: Long): Int

public external fun sqlite3_backup_step(
    backup: Long,
    nPage: Int,
): Int

public external fun sqlite3_bind_blob(
    stmt: Long,
    index: Int,
    data: ByteArray?,
    size: Int,
    destructor: DestructorCallback?
): Int

public external fun sqlite3_bind_blob64(
    stmt: Long,
    index: Int,
    data: ByteBuffer?,
    size: Long,
    destructor: DestructorCallback?
): Int

public external fun sqlite3_bind_double(
    stmt: Long,
    index: Int,
    value: Double
): Int

public external fun sqlite3_bind_int(
    stmt: Long,
    index: Int,
    value: Int
): Int

public external fun sqlite3_bind_int64(
    stmt: Long,
    index: Int,
    value: Long
): Int

public external fun sqlite3_bind_null(
    stmt: Long,
    index: Int
): Int

public external fun sqlite3_bind_parameter_count(
    stmt: Long,
): Int

public external fun sqlite3_bind_parameter_index(
    stmt: Long,
    name: String
): Int

public external fun sqlite3_bind_parameter_name(
    stmt: Long,
    index: Int
): String

public external fun sqlite3_bind_pointer(
    stmt: Long,
    index: Int,
    data: Any?,
    type: String?,
    destructor: DestructorCallback?
): Int

public external fun sqlite3_bind_text(
    stmt: Long,
    index: Int,
    text: String?,
    size: Int,
    computeSize: Boolean
): Int

public external fun sqlite3_bind_text64(
    stmt: Long,
    index: Int,
    data: ByteBuffer?,
    size: Long,
    destructor: DestructorCallback?,
    encoding: Int
): Int

public external fun sqlite3_bind_value(
    stmt: Long,
    index: Int,
    value: Long
): Int

public external fun sqlite3_bind_zeroblob(
    stmt: Long,
    index: Int,
    size: Int
): Int

public external fun sqlite3_bind_zeroblob64(
    stmt: Long,
    index: Int,
    size: Long
): Int

public external fun sqlite3_blob_bytes(blob: Long): Int

public external fun sqlite3_blob_close(blob: Long): Int

public external fun sqlite3_blob_open(
    db: Long,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Int,
    outBlob: OutputPointer.OfPointer?,
): Int

public external fun sqlite3_blob_read(
    blob: Long,
    buffer: ByteArray,
    size: Int,
    offset: Int,
): Int

public external fun sqlite3_blob_reopen(
    blob: Long,
    rowIndex: Long,
): Int

public external fun sqlite3_blob_write(
    blob: Long,
    buffer: ByteArray,
    size: Int,
    offset: Int,
): Int

public external fun sqlite3_busy_handler(
    db: Long,
    callback: BusyHandlerCallback?,
): Int

public external fun sqlite3_busy_timeout(
    db: Long,
    millis: Int,
): Int

public external fun sqlite3_changes(db: Long): Int

public external fun sqlite3_changes64(db: Long): Long

public external fun sqlite3_clear_bindings(stmt: Long): Int

public external fun sqlite3_close(db: Long): Int

public external fun sqlite3_close_v2(db: Long): Int

public external fun sqlite3_collation_needed(
    db: Long,
    callback: CollationNeededCallback?,
): Int

public external fun sqlite3_column_blob(
    stmt: Long,
    index: Int,
): ByteArray

public external fun sqlite3_column_bytes(
    stmt: Long,
    index: Int,
): Int

public external fun sqlite3_column_count(stmt: Long): Int

public external fun sqlite3_column_database_name(
    stmt: Long,
    index: Int,
): String?

public external fun sqlite3_column_decltype(
    stmt: Long,
    index: Int,
): String?

public external fun sqlite3_column_double(
    stmt: Long,
    index: Int,
): Double

public external fun sqlite3_column_int(
    stmt: Long,
    index: Int,
): Int

public external fun sqlite3_column_int64(
    stmt: Long,
    index: Int,
): Long

public external fun sqlite3_column_name(
    stmt: Long,
    index: Int,
): String?

public external fun sqlite3_column_origin_name(
    stmt: Long,
    index: Int,
): String?

public external fun sqlite3_column_table_name(
    stmt: Long,
    index: Int,
): String?

public external fun sqlite3_column_text(
    stmt: Long,
    index: Int,
): String?

public external fun sqlite3_column_type(
    stmt: Long,
    index: Int,
): Int

public external fun sqlite3_column_value(
    stmt: Long,
    index: Int,
): Long

/*public external fun sqlite3_commit_hook(
    p0: Long,
    p1: Long,
    p2: Long,
): Long

public external fun sqlite3_compileoption_get(
    p0: Int,
): Long

public external fun sqlite3_compileoption_used(
    p0: Long,
): Int

public external fun sqlite3_complete(
    p0: Long,
): Int

/**
 * TODO variadic
 */
public external fun sqlite3_config(
    p0: Int,
    p1: Long,
): Int

public external fun sqlite3_context_db_handle(
    p0: Long,
): Long

public external fun sqlite3_create_collation(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Long,
    p4: Long,
): Int

public external fun sqlite3_create_collation_v2(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Long,
    p4: Long,
    p5: Long,
): Int

public external fun sqlite3_create_function(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Int,
    p4: Long,
    p5: Long,
    p6: Long,
    p7: Long,
): Int

public external fun sqlite3_create_function_v2(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Int,
    p4: Long,
    p5: Long,
    p6: Long,
    p7: Long,
    p8: Long,
): Int

public external fun sqlite3_create_module(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Long,
): Int

public external fun sqlite3_create_module_v2(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Long,
    p4: Long,
): Int

public external fun sqlite3_create_window_function(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Int,
    p4: Long,
    p5: Long,
    p6: Long,
    p7: Long,
    p8: Long,
    p9: Long,
): Int

public external fun sqlite3_data_count(
    p0: Long,
): Int

public external fun sqlite3_db_cacheflush(
    p0: Long,
): Int

/**
 * TODO variadic
 */
public external fun sqlite3_db_config(
    p0: Long,
    p1: Int,
    p2: Long,
): Int

public external fun sqlite3_db_filename(
    p0: Long,
    p1: Long,
): Long

public external fun sqlite3_db_handle(
    p0: Long,
): Long

public external fun sqlite3_db_name(
    p0: Long,
    p1: Int,
): Long

public external fun sqlite3_db_readonly(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_db_release_memory(
    p0: Long,
): Int

public external fun sqlite3_db_status(
    p0: Long,
    p1: Int,
    p2: Long,
    p3: Long,
    p4: Int,
): Int

public external fun sqlite3_db_status64(
    p0: Long,
    p1: Int,
    p2: Long,
    p3: Long,
    p4: Int,
): Int

public external fun sqlite3_declare_vtab(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_deserialize(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Long,
    p4: Long,
    p5: Int,
): Int

public external fun sqlite3_drop_modules(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_errcode(
    p0: Long,
): Int

public external fun sqlite3_errmsg(
    p0: Long,
): Long

public external fun sqlite3_error_offset(
    p0: Long,
): Int

public external fun sqlite3_errstr(
    p0: Int,
): Long

public external fun sqlite3_exec(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Long,
    p4: Long,
): Int

public external fun sqlite3_expanded_sql(
    p0: Long,
): Long

public external fun sqlite3_extended_errcode(
    p0: Long,
): Int

public external fun sqlite3_extended_result_codes(
    p0: Long,
    p1: Int,
): Int

public external fun sqlite3_file_control(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Long,
): Int

public external fun sqlite3_finalize(
    p0: Long,
): Int

public external fun sqlite3_free(
    p0: Long,
)

public external fun sqlite3_get_autocommit(
    p0: Long,
): Int

public external fun sqlite3_get_auxdata(
    p0: Long,
    p1: Int,
): Long

public external fun sqlite3_initialize(): Int

public external fun sqlite3_interrupt(
    p0: Long,
)

public external fun sqlite3_is_interrupted(
    p0: Long,
): Int

public external fun sqlite3_key(
    p0: Long,
    p1: Long,
    p2: Int,
): Int

public external fun sqlite3_key_v2(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Int,
): Int

public external fun sqlite3_keyword_check(
    p0: Long,
    p1: Int,
): Int

public external fun sqlite3_keyword_count(): Int

public external fun sqlite3_keyword_name(
    p0: Int,
    p1: Long,
    p2: Long,
): Int

public external fun sqlite3_last_insert_rowid(
    p0: Long,
): Long

public external fun sqlite3_libversion(): Long

public external fun sqlite3_libversion_number(): Int

public external fun sqlite3_limit(
    p0: Long,
    p1: Int,
    p2: Int,
): Int

/**
 * TODO variadic
 */
public external fun sqlite3_log(
    p0: Int,
    p1: Long,
    p2: Long
)

public external fun sqlite3_malloc(
    p0: Int,
): Long

public external fun sqlite3_malloc64(
    p0: Long,
): Long

public external fun sqlite3_msize(
    p0: Long,
): Long

public external fun sqlite3_memory_used(
): Long

public external fun sqlite3_memory_highwater(
    p0: Int,
): Long

public external fun sqlite3_next_stmt(
    p0: Long,
    p1: Long,
): Long

public external fun sqlite3_open(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_open_v2(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Long,
): Int

public external fun sqlite3_overload_function(
    p0: Long,
    p1: Long,
    p2: Int,
): Int

public external fun sqlite3_prepare_v2(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Long,
    p4: Long,
): Int

public external fun sqlite3_prepare_v3(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Int,
    p4: Long,
    p5: Long,
): Int

public external fun sqlite3_preupdate_blobwrite(
    p0: Long,
): Int

public external fun sqlite3_preupdate_count(
    p0: Long,
): Int

public external fun sqlite3_preupdate_depth(
    p0: Long,
): Int

public external fun sqlite3_preupdate_hook(
    p0: Long,
    p1: Long,
    p2: Long,
): Long

public external fun sqlite3_preupdate_new(
    p0: Long,
    p1: Int,
    p2: Long,
): Int

public external fun sqlite3_preupdate_old(
    p0: Long,
    p1: Int,
    p2: Long,
): Int

public external fun sqlite3_progress_handler(
    p0: Long,
    p1: Int,
    p2: Long,
    p3: Long,
)

public external fun sqlite3_randomness(
    p0: Int,
    p1: Long,
)

public external fun sqlite3_realloc(
    p0: Long,
    p1: Int,
): Long

public external fun sqlite3_realloc64(
    p0: Long,
    p1: Long,
): Long

public external fun sqlite3_rekey(
    p0: Long,
    p1: Long,
    p2: Int,
): Int

public external fun sqlite3_rekey_v2(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Int,
): Int

public external fun sqlite3_release_memory(
    p0: Int,
): Int

public external fun sqlite3_reset(
    p0: Long,
): Int

public external fun sqlite3_reset_auto_extension()

public external fun sqlite3_result_blob(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Long,
)

public external fun sqlite3_result_blob64(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Long,)

public external fun sqlite3_result_double(
    p0: Long,
    p1: Double,
)

public external fun sqlite3_result_error(
    p0: Long,
    p1: Long,
    p2: Int,
)

public external fun sqlite3_result_error_code(
    p0: Long,
    p1: Int,
)

public external fun sqlite3_result_error_nomem(
    p0: Long,
)

public external fun sqlite3_result_error_toobig(
    p0: Long,
)

public external fun sqlite3_result_int(
    p0: Long,
    p1: Int,
)

public external fun sqlite3_result_int64(
    p0: Long,
    p1: Long,
)

public external fun sqlite3_result_null(
    p0: Long,
)

public external fun sqlite3_result_pointer(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Long,
)

public external fun sqlite3_result_subtype(
    p0: Long,
    p1: Int,
)

public external fun sqlite3_result_text(
    p0: Long,
    p1: Long,
    p2: Int,
    p3: Long,
)

public external fun sqlite3_result_text64(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Long,
    p4: Int,)

public external fun sqlite3_result_zeroblob(
    p0: Long,
    p1: Int,
)

public external fun sqlite3_result_zeroblob64(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_result_value(
    p0: Long,
    p1: Long,)

public external fun sqlite3_rollback_hook(
    p0: Long,
    p1: Long,
    p2: Long,
): Long

public external fun sqlite3_serialize(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Int,
): Long

public external fun sqlite3_set_authorizer(
    p0: Long,
    p1: Long,
    p2: Long,
): Int

public external fun sqlite3_set_auxdata(
    p0: Long,
    p1: Int,
    p2: Long,
    p3: Long,
)

public external fun sqlite3_set_errmsg(
    p0: Long,
    p1: Int,
    p2: Long,
): Int

public external fun sqlite3_set_last_insert_rowid(
    p0: Long,
    p1: Long,
)

public external fun sqlite3_shutdown(): Int

public external fun sqlite3_sourceid(): Long

public external fun sqlite3_sql(
    p0: Long,
): Long

public external fun sqlite3_status(
    p0: Int,
    p1: Long,
    p2: Long,
    p3: Int,
): Int

public external fun sqlite3_status64(
    p0: Int,
    p1: Long,
    p2: Long,
    p3: Int,
): Int

public external fun sqlite3_step(
    p0: Long,
): Int

public external fun Long_busy(
    p0: Long,
): Int

public external fun Long_explain(
    p0: Long,
    p1: Int,
): Int

public external fun Long_isexplain(
    p0: Long,
): Int

public external fun Long_readonly(
    p0: Long,
): Int

public external fun Long_status(
    p0: Long,
    p1: Int,
    p2: Int,
): Int

public external fun sqlite3_strglob(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_stricmp(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_strlike(
    p0: Long,
    p1: Long,
    p2: Int,
): Int

public external fun sqlite3_strnicmp(
    p0: Long,
    p1: Long,
    p2: Int,
): Int

public external fun sqlite3_system_errno(
    p0: Long,
): Int

public external fun sqlite3_table_column_metadata(
    p0: Long,
    p1: Long,
    p2: Long,
    p3: Long,
    p4: Long,
    p5: Long,
    p6: Long,
    p7: Long,
    p8: Long,
): Int

public external fun sqlite3_total_changes(
    p0: Long,
): Int

public external fun sqlite3_total_changes64(
    p0: Long,
): Long

public external fun sqlite3_trace_v2(
    p0: Long,
    p1: Int,
    p2: Long,
    p3: Long,
): Int

public external fun sqlite3_txn_state(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_update_hook(
    p0: Long,
    p1: Long,
    p2: Long,
): Long

public external fun sqlite3_uri_boolean(
    p0: Long,
    p1: Long,
    p2: Int,
): Int

public external fun sqlite3_uri_int64(
    p0: Long,
    p1: Long,
    p2: Long,
): Long

public external fun sqlite3_uri_key(
    p0: Long,
    p1: Int,
): Long

public external fun sqlite3_uri_parameter(
    p0: Long,
    p1: Long,
): Long

public external fun sqlite3_user_data(
    p0: Long,
): Long

public external fun Long_blob(
    p0: Long,
): Long

public external fun Long_bytes(
    p0: Long,
): Int

public external fun Long_double(
    p0: Long,
): Double

public external fun Long_dup(
    p0: Long,
): Long

public external fun Long_encoding(
    p0: Long,
): Int

public external fun Long_free(
    p0: Long,
)

public external fun Long_frombind(
    p0: Long,
): Int

public external fun Long_int(
    p0: Long,
): Int

public external fun Long_int64(
    p0: Long,
): Long

public external fun Long_nochange(
    p0: Long,
): Int

public external fun Long_numeric_type(
    p0: Long,
): Int

public external fun Long_pointer(
    p0: Long,
    p1: Long,
): Long

public external fun Long_subtype(
    p0: Long,
): Int

public external fun Long_text(
    p0: Long,
): Long

public external fun Long_type(
    p0: Long,
): Int

public external fun sqlite3_vfs_find(
    p0: Long,
): Long

public external fun sqlite3_vfs_register(
    p0: Long,
    p1: Int,
): Int

public external fun sqlite3_vfs_unregister(
    p0: Long,
): Int

public external fun sqlite3_vtab_collation(
    p0: Long,
    p1: Int,
): Long

public external fun sqlite3_vtab_config(
    p0: Long,
    p1: Int,
    p2: Long,
): Int

public external fun sqlite3_vtab_distinct(
    p0: Long,
): Int

public external fun sqlite3_vtab_in(
    p0: Long,
    p1: Int,
    p2: Int,
): Int

public external fun sqlite3_vtab_in_first(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_vtab_in_next(
    p0: Long,
    p1: Long,
): Int

public external fun sqlite3_vtab_nochange(
    p0: Long,
): Int

public external fun sqlite3_vtab_on_conflict(
    p0: Long,
): Int

public external fun sqlite3_vtab_rhs_value(
    p0: Long,
    p1: Int,
    p2: Long,
): Int
 */