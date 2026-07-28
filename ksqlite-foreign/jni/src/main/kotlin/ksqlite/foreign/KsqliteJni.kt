/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:JvmName("KsqliteJni")
@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.foreign

import ksqlite.foreign.callbacks.AuthorizerCallback
import ksqlite.foreign.callbacks.AutoExtensionCallback
import ksqlite.foreign.callbacks.AutovacuumPagesCallback
import ksqlite.foreign.callbacks.BusyHandlerCallback
import ksqlite.foreign.callbacks.CipherDescriptorCallbacks
import ksqlite.foreign.callbacks.CollationCallback
import ksqlite.foreign.callbacks.CollationNeededCallback
import ksqlite.foreign.callbacks.CommitHookCallback
import ksqlite.foreign.callbacks.DestructorCallback
import ksqlite.foreign.callbacks.ExecCallback
import ksqlite.foreign.callbacks.FunctionCallback
import ksqlite.foreign.callbacks.PreupdateHookCallback
import ksqlite.foreign.callbacks.ProgressHandlerCallback
import ksqlite.foreign.callbacks.RollbackHookCallback
import ksqlite.foreign.callbacks.TraceCallback
import ksqlite.foreign.callbacks.UpdateHookCallback
import ksqlite.foreign.callbacks.VtabModuleCallbacks
import ksqlite.foreign.callbacks.WalHookCallback
import ksqlite.foreign.structs.JniStructLayoutProvider
import ksqlite.structs.StructLayout
import ksqlite.structs.setStructLayoutProvider
import java.nio.ByteBuffer

///////////////////////////////////////////////////////////////////////////
// Library
///////////////////////////////////////////////////////////////////////////

/**
 * Loads the Kotlin SQLite library (including SQLite Official JNI layer and SQLite).
 */
public fun ksqliteLoadLibrary() {
    System.loadLibrary(KSQLITE_NATIVE_LIB_NAME)
    setStructLayoutProvider(JniStructLayoutProvider)
}

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Allocates [size] bytes and returns a pointer to the allocated memory.
 * The default allocator is used to obtains memory.
 */
public external fun nativeBufferAllocate(size: Long): JniPointer

/**
 * Frees memory previously obtained using [nativeBufferAllocate].
 */
public external fun nativeBufferFree(pointer: JniPointer)

/**
 * Reads bytes into [destination].
 */
public external fun nativeBufferRead(
    buffer: JniPointer,
    destination: ByteArray,
    size: Int,
    sourceOffset: Long,
    destinationOffset: Int
)

/**
 * Writes bytes from [source].
 */
public external fun nativeBufferWrite(
    buffer: JniPointer,
    source: ByteArray,
    size: Int,
    sourceOffset: Int,
    destinationOffset: Long
)

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Reads bytes until null termination marker is found and returns the bytes read as [String].
 */
public external fun nativeReadString(pointer: JniPointer): String

/**
 * Frees [pointer] using `sqlite3_free` ands returns the result of `sqlite3_mprintf` on [message].
 * If [message] is `null` then only [sqlite3_free] is called on [pointer] and `0` is returned
 */
public external fun nativeFreeAndMalloc(
    pointer: JniPointer,
    message: String?
): JniPointer

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the layout of the struct.
 */
private external fun nativeStructLayout(type: Int): StructLayout

internal fun structLayout(type: Int): StructLayout = nativeStructLayout(type)

/**
 * Returns a writable view of the struct as a [ByteBuffer] pointing to [pointer].
 */
private external fun nativeStructReinterpret(
    size: Int,
    pointer: JniPointer
): ByteBuffer

internal fun structReinterpret(
    size: Int,
    pointer: JniPointer
): ByteBuffer = nativeStructReinterpret(size, pointer)

/**
 * Allocates a new struct with given [size] and returns a writable view of the struct as a
 * [ByteBuffer] pointing to the address of the struct.
 *
 * The allocated struct address is written into [pointer].
 * The returned memory region is zeroized.
 */
private external fun nativeStructMalloc(
    size: Int,
    pointer: OutputPointer.OfPointer
): ByteBuffer

internal fun structMalloc(
    size: Int,
    pointer: OutputPointer.OfPointer
): ByteBuffer = nativeStructMalloc(size, pointer)

/**
 * Frees a struct at the address of [buffer].
 */
private external fun nativeStructFree(buffer: ByteBuffer)

internal fun structFree(buffer: ByteBuffer) = nativeStructFree(buffer)

///////////////////////////////////////////////////////////////////////////
// SQLite
///////////////////////////////////////////////////////////////////////////

/**
 * To align with other platforms behavior, only one auto extension is allowed.
 */
public external fun ksqlite_auto_extension(callback: AutoExtensionCallback): Int

public external fun ksqlite_cancel_auto_extension(callback: AutoExtensionCallback): Int

public external fun ksqlite_prepare_v2(
    db: JniPointer,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: OutputPointer.OfPointer,
    outOffset: OutputPointer.OfInt32?
): Int

public external fun ksqlite_prepare_v3(
    db: JniPointer,
    sql: ByteArray,
    maxBytes: Int,
    flags: Int,
    outStmt: OutputPointer.OfPointer,
    outOffset: OutputPointer.OfInt32?
): Int

public external fun sqlite3_aggregate_context(
    context: JniPointer,
    create: Boolean
): JniPointer

public external fun sqlite3_autovacuum_pages(
    db: JniPointer,
    callback: AutovacuumPagesCallback?,
    destructor: DestructorCallback?,
): Int

public external fun sqlite3_backup_finish(backup: JniPointer): Int

public external fun sqlite3_backup_init(
    destDb: JniPointer,
    destDbName: String,
    srcDb: JniPointer,
    srcDbName: String
): JniPointer

public external fun sqlite3_backup_pagecount(backup: JniPointer): Int

public external fun sqlite3_backup_remaining(backup: JniPointer): Int

public external fun sqlite3_backup_step(
    backup: JniPointer,
    nPage: Int,
): Int

public external fun sqlite3_bind_blob(
    stmt: JniPointer,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destructor: DestructorCallback?
): Int

public external fun sqlite3_bind_blob64(
    stmt: JniPointer,
    index: Int,
    buffer: JniPointer,
    size: Long,
    destructor: DestructorCallback?
): Int

public external fun sqlite3_bind_double(
    stmt: JniPointer,
    index: Int,
    value: Double
): Int

public external fun sqlite3_bind_int(
    stmt: JniPointer,
    index: Int,
    value: Int
): Int

public external fun sqlite3_bind_int64(
    stmt: JniPointer,
    index: Int,
    value: Long
): Int

public external fun sqlite3_bind_null(
    stmt: JniPointer,
    index: Int
): Int

public external fun sqlite3_bind_parameter_count(
    stmt: JniPointer,
): Int

public external fun sqlite3_bind_parameter_index(
    stmt: JniPointer,
    name: String
): Int

public external fun sqlite3_bind_parameter_name(
    stmt: JniPointer,
    index: Int
): String

public external fun sqlite3_bind_pointer(
    stmt: JniPointer,
    index: Int,
    data: Any?,
    type: String?,
    destructor: DestructorCallback?
): Int

public external fun sqlite3_bind_text(
    stmt: JniPointer,
    index: Int,
    value: String
): Int

public external fun sqlite3_bind_text64(
    stmt: JniPointer,
    index: Int,
    buffer: JniPointer,
    size: Long,
    destructor: DestructorCallback?,
    encoding: Int
): Int

public external fun sqlite3_bind_value(
    stmt: JniPointer,
    index: Int,
    value: JniPointer
): Int

public external fun sqlite3_bind_zeroblob(
    stmt: JniPointer,
    index: Int,
    size: Int
): Int

public external fun sqlite3_bind_zeroblob64(
    stmt: JniPointer,
    index: Int,
    size: Long
): Int

public external fun sqlite3_blob_bytes(blob: JniPointer): Int

public external fun sqlite3_blob_close(blob: JniPointer): Int

public external fun sqlite3_blob_open(
    db: JniPointer,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowid: Long,
    flags: Int,
    outBlob: OutputPointer.OfPointer?,
): Int

public external fun sqlite3_blob_read(
    blob: JniPointer,
    buffer: ByteArray,
    size: Int,
    offset: Int,
): Int

public external fun sqlite3_blob_reopen(
    blob: JniPointer,
    rowid: Long,
): Int

public external fun sqlite3_blob_write(
    blob: JniPointer,
    buffer: ByteArray,
    size: Int,
    offset: Int,
): Int

public external fun sqlite3_busy_handler(
    db: JniPointer,
    callback: BusyHandlerCallback?,
): Int

public external fun sqlite3_busy_timeout(
    db: JniPointer,
    millis: Int,
): Int

public external fun sqlite3_changes(db: JniPointer): Int

public external fun sqlite3_changes64(db: JniPointer): Long

public external fun sqlite3_clear_bindings(stmt: JniPointer): Int

public external fun sqlite3_close(db: JniPointer): Int

public external fun sqlite3_close_v2(db: JniPointer): Int

public external fun sqlite3_collation_needed(
    db: JniPointer,
    callback: CollationNeededCallback?,
): Int

public external fun sqlite3_column_blob(
    stmt: JniPointer,
    index: Int,
): ByteArray

public external fun sqlite3_column_buffer(
    stmt: JniPointer,
    index: Int,
    outSize: OutputPointer.OfInt64
): JniPointer

public external fun sqlite3_column_bytes(
    stmt: JniPointer,
    index: Int,
): Int

public external fun sqlite3_column_count(stmt: JniPointer): Int

public external fun sqlite3_column_database_name(
    stmt: JniPointer,
    index: Int,
): String?

public external fun sqlite3_column_decltype(
    stmt: JniPointer,
    index: Int,
): String?

public external fun sqlite3_column_double(
    stmt: JniPointer,
    index: Int,
): Double

public external fun sqlite3_column_int(
    stmt: JniPointer,
    index: Int,
): Int

public external fun sqlite3_column_int64(
    stmt: JniPointer,
    index: Int,
): Long

public external fun sqlite3_column_name(
    stmt: JniPointer,
    index: Int,
): String?

public external fun sqlite3_column_origin_name(
    stmt: JniPointer,
    index: Int,
): String?

public external fun sqlite3_column_table_name(
    stmt: JniPointer,
    index: Int,
): String?

public external fun sqlite3_column_text(
    stmt: JniPointer,
    index: Int,
): String?

public external fun sqlite3_column_type(
    stmt: JniPointer,
    index: Int,
): Int

public external fun sqlite3_column_value(
    stmt: JniPointer,
    index: Int,
): JniPointer

public external fun sqlite3_commit_hook(
    db: JniPointer,
    callback: CommitHookCallback?
): CommitHookCallback?

public external fun sqlite3_compileoption_get(index: Int): String?

public external fun sqlite3_compileoption_used(optName: String): Int

public external fun sqlite3_complete(sql: String): Int

public external fun sqlite3_config(
    id: Int,
    args: Array<*>
): Int

public external fun sqlite3_context_db_handle(context: JniPointer): JniPointer

public external fun sqlite3_create_collation_v2(
    db: JniPointer,
    name: String,
    eTextRep: Int,
    destructor: DestructorCallback?,
    callback: CollationCallback?
): Int

public external fun sqlite3_create_function_v2(
    db: JniPointer,
    name: String,
    nArg: Int,
    eTextRep: Int,
    appData: Any?,
    func: FunctionCallback.Func?,
    step: FunctionCallback.Step?,
    final: FunctionCallback.Final?,
    destroy: DestructorCallback?,
): Int

public external fun sqlite3_create_module_v2(
    db: JniPointer,
    name: String,
    module: JniPointer,
    appData: Any?,
    destroy: DestructorCallback?,
): Int

public external fun sqlite3_create_window_function(
    db: JniPointer,
    name: String,
    nArg: Int,
    eTextRep: Int,
    appData: Any?,
    step: FunctionCallback.Step?,
    final: FunctionCallback.Final?,
    value: FunctionCallback.Value?,
    inverse: FunctionCallback.Inverse?,
    destroy: DestructorCallback?,
): Int

public external fun sqlite3_data_count(stmt: JniPointer): Int

public external fun sqlite3_db_cacheflush(db: JniPointer): Int

public external fun sqlite3_db_config(
    db: JniPointer,
    option: Int,
    args: Array<*>
): Int

public external fun sqlite3_db_filename(
    db: JniPointer,
    name: String
): JniPointer

public external fun sqlite3_db_handle(stmt: JniPointer): JniPointer

public external fun sqlite3_db_name(
    db: JniPointer,
    index: Int
): String?

public external fun sqlite3_db_readonly(
    db: JniPointer,
    name: String
): Int

public external fun sqlite3_db_release_memory(
    db: JniPointer,
): Int

public external fun sqlite3_db_status(
    db: JniPointer,
    option: Int,
    outCurrent: OutputPointer.OfInt32?,
    outHighwater: OutputPointer.OfInt32?,
    resetFlag: Int,
): Int

public external fun sqlite3_db_status64(
    db: JniPointer,
    option: Int,
    outCurrent: OutputPointer.OfInt64?,
    outHighwater: OutputPointer.OfInt64?,
    resetFlag: Int,
): Int

public external fun sqlite3_declare_vtab(
    db: JniPointer,
    sql: String
): Int

public external fun sqlite3_deserialize(
    db: JniPointer,
    schema: String?,
    buffer: JniPointer,
    dbSize: Long,
    bufferSize: Long,
    flags: Int,
): Int

public external fun sqlite3_drop_modules(
    db: JniPointer,
    modules: Array<String>?
): Int

public external fun sqlite3_errcode(db: JniPointer): Int

public external fun sqlite3_errmsg(db: JniPointer): String?

public external fun sqlite3_error_offset(db: JniPointer): Int

public external fun sqlite3_errstr(resultCode: Int): String?

public external fun sqlite3_exec(
    db: JniPointer,
    sql: String,
    callback: ExecCallback?,
    errorMessage: OutputPointer.OfString?
): Int

public external fun sqlite3_expanded_sql(stmt: JniPointer): String?

public external fun sqlite3_extended_errcode(db: JniPointer): Int

public external fun sqlite3_extended_result_codes(
    db: JniPointer,
    enabled: Int,
): Int

public external fun sqlite3_file_control(
    db: JniPointer,
    name: String?,
    opcode: Int,
    param: Any?
): Int

public external fun sqlite3_filename_database(fileName: JniPointer): String?

public external fun sqlite3_filename_journal(fileName: JniPointer): String?

public external fun sqlite3_filename_wal(fileName: JniPointer): String?

public external fun sqlite3_finalize(stmt: JniPointer): Int

public external fun sqlite3_free(buffer: JniPointer)

public external fun sqlite3_get_autocommit(db: JniPointer): Int

public external fun sqlite3_hard_heap_limit64(limit: Long): Long

public external fun sqlite3_get_auxdata(
    context: JniPointer,
    index: Int,
): JniPointer

public external fun sqlite3_initialize(): Int

public external fun sqlite3_interrupt(db: JniPointer)

public external fun sqlite3_is_interrupted(db: JniPointer): Int

public external fun sqlite3_key(
    db: JniPointer,
    key: ByteArray,
    nKey: Int,
): Int

public external fun sqlite3_key_v2(
    db: JniPointer,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Int

public external fun sqlite3_keyword_check(word: String): Int

public external fun sqlite3_keyword_count(): Int

public external fun sqlite3_keyword_name(
    index: Int,
    name: OutputPointer.OfString?
): Int

public external fun sqlite3_last_insert_rowid(db: JniPointer): Long

public external fun sqlite3_libversion(): String

public external fun sqlite3_libversion_number(): Int

public external fun sqlite3_limit(
    db: JniPointer,
    id: Int,
    newVal: Int
): Int

public external fun sqlite3_log(
    errorCode: Int,
    message: String
)

public external fun sqlite3_malloc(size: Int): JniPointer

public external fun sqlite3_malloc64(size: Long): JniPointer

public external fun sqlite3_memory_used(): Long

public external fun sqlite3_memory_highwater(resetFlag: Int): Long

public external fun sqlite3_msize(buffer: JniPointer): Long

public external fun sqlite3_next_stmt(
    db: JniPointer,
    stmt: JniPointer,
): JniPointer

public external fun sqlite3_open(
    fileName: String,
    outDb: OutputPointer.OfPointer,
): Int

public external fun sqlite3_open_v2(
    fileName: String,
    outDb: OutputPointer.OfPointer,
    flags: Int,
    vfs: String?,
): Int

public external fun sqlite3_overload_function(
    db: JniPointer,
    name: String,
    nArg: Int
): Int

public external fun sqlite3_prepare_v2(
    db: JniPointer,
    sql: String,
    outStmt: OutputPointer.OfPointer
): Int

public external fun sqlite3_prepare_v3(
    db: JniPointer,
    sql: String,
    flags: Int,
    outStmt: OutputPointer.OfPointer
): Int

public external fun sqlite3_preupdate_blobwrite(db: JniPointer): Int

public external fun sqlite3_preupdate_count(db: JniPointer): Int

public external fun sqlite3_preupdate_depth(db: JniPointer): Int

public external fun sqlite3_preupdate_hook(
    db: JniPointer,
    callback: PreupdateHookCallback?
): PreupdateHookCallback?

public external fun sqlite3_preupdate_new(
    db: JniPointer,
    index: Int,
    outValue: OutputPointer.OfPointer,
): Int

public external fun sqlite3_preupdate_old(
    db: JniPointer,
    index: Int,
    outValue: OutputPointer.OfPointer,
): Int

public external fun sqlite3_progress_handler(
    db: JniPointer,
    nOps: Int,
    callback: ProgressHandlerCallback?
)

public external fun sqlite3_randomness(
    size: Int,
    buffer: JniPointer,
)

public external fun sqlite3_realloc(
    buffer: JniPointer,
    size: Int,
): JniPointer

public external fun sqlite3_realloc64(
    buffer: JniPointer,
    size: Long,
): JniPointer

public external fun sqlite3_rekey(
    db: JniPointer,
    key: ByteArray,
    nKey: Int,
): Int

public external fun sqlite3_rekey_v2(
    db: JniPointer,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Int

public external fun sqlite3_release_memory(size: Int): Int

public external fun sqlite3_reset(stmt: JniPointer): Int

public external fun sqlite3_reset_auto_extension()

public external fun sqlite3_result_blob(
    context: JniPointer,
    bytes: ByteArray,
    size: Int,
    destructor: DestructorCallback?
)

public external fun sqlite3_result_blob64(
    context: JniPointer,
    buffer: JniPointer,
    size: Long,
    destructor: DestructorCallback?
)

public external fun sqlite3_result_double(
    context: JniPointer,
    value: Double,
)

public external fun sqlite3_result_error(
    context: JniPointer,
    message: String
)

public external fun sqlite3_result_error_code(
    context: JniPointer,
    errorCode: Int,
)

public external fun sqlite3_result_error_nomem(context: JniPointer)

public external fun sqlite3_result_error_toobig(context: JniPointer)

public external fun sqlite3_result_int(
    context: JniPointer,
    value: Int,
)

public external fun sqlite3_result_int64(
    context: JniPointer,
    value: Long,
)

public external fun sqlite3_result_null(context: JniPointer)

public external fun sqlite3_result_pointer(
    context: JniPointer,
    data: Any?,
    type: String?,
    destructor: DestructorCallback?
)

public external fun sqlite3_result_subtype(
    context: JniPointer,
    subtype: Int,
)

public external fun sqlite3_result_text(
    context: JniPointer,
    value: String
)

public external fun sqlite3_result_text64(
    context: JniPointer,
    buffer: JniPointer,
    size: Long,
    destructor: DestructorCallback?,
    encoding: Int
)

public external fun sqlite3_result_value(
    context: JniPointer,
    value: JniPointer
)

public external fun sqlite3_result_zeroblob(
    context: JniPointer,
    size: Int,
)

public external fun sqlite3_result_zeroblob64(
    context: JniPointer,
    size: Long,
): Int

public external fun sqlite3_rollback_hook(
    db: JniPointer,
    callback: RollbackHookCallback?
): RollbackHookCallback?

public external fun sqlite3_serialize(
    db: JniPointer,
    schema: String?,
    outSize: OutputPointer.OfInt64,
    flags: Int,
): JniPointer

public external fun sqlite3_set_authorizer(
    db: JniPointer,
    callback: AuthorizerCallback?
): Int

public external fun sqlite3_set_auxdata(
    context: JniPointer,
    index: Int,
    destructor: DestructorCallback?
): JniPointer

public external fun sqlite3_set_errmsg(
    db: JniPointer,
    errorCode: Int,
    message: String?
): Int

public external fun sqlite3_set_last_insert_rowid(
    db: JniPointer,
    rowid: Long,
)

public external fun sqlite3_shutdown(): Int

public external fun sqlite3_snapshot_cmp(
    snapshot1: JniPointer,
    snapshot2: JniPointer,
): Int

public external fun sqlite3_snapshot_free(snapshot: JniPointer)

public external fun sqlite3_snapshot_get(
    db: JniPointer,
    name: String?,
    outSnapshot: OutputPointer.OfPointer
): Int

public external fun sqlite3_snapshot_open(
    db: JniPointer,
    name: String?,
    snapshot: JniPointer
): Int

public external fun sqlite3_snapshot_recover(
    db: JniPointer,
    name: String?
): Int

public external fun sqlite3_soft_heap_limit64(limit: Long): Long

public external fun sqlite3_sourceid(): String

public external fun sqlite3_sql(stmt: JniPointer): String

public external fun sqlite3_status(
    option: Int,
    outCurrent: OutputPointer.OfInt32,
    outHighwater: OutputPointer.OfInt32,
    resetFlag: Int,
): Int

public external fun sqlite3_status64(
    option: Int,
    outCurrent: OutputPointer.OfInt64,
    outHighwater: OutputPointer.OfInt64,
    resetFlag: Int,
): Int

public external fun sqlite3_step(stmt: JniPointer): Int

public external fun sqlite3_stmt_busy(stmt: JniPointer): Int

public external fun sqlite3_stmt_explain(
    stmt: JniPointer,
    mode: Int,
): Int

public external fun sqlite3_stmt_isexplain(stmt: JniPointer): Int

public external fun sqlite3_stmt_readonly(stmt: JniPointer): Int

public external fun sqlite3_stmt_status(
    stmt: JniPointer,
    counter: Int,
    resetFlag: Int,
): Int

public external fun sqlite3_strglob(
    pattern: String,
    input: String,
): Int

public external fun sqlite3_stricmp(
    first: String,
    second: String
): Int

public external fun sqlite3_strlike(
    pattern: String,
    input: String,
    escape: Int,
): Int

public external fun sqlite3_strnicmp(
    first: String,
    second: String,
    maxChars: Int,
): Int

public external fun sqlite3_system_errno(db: JniPointer): Int

public external fun sqlite3_table_column_metadata(
    db: JniPointer,
    dbName: String?,
    tableName: String,
    columnName: String,
    outDataType: OutputPointer.OfString?,
    outCollationName: OutputPointer.OfString?,
    outNotNull: OutputPointer.OfInt32?,
    outPrimaryKey: OutputPointer.OfInt32?,
    outAutoIncrement: OutputPointer.OfInt32?
): Int

public external fun sqlite3_threadsafe(): Int

public external fun sqlite3_total_changes(db: JniPointer): Int

public external fun sqlite3_total_changes64(db: JniPointer): Long

public external fun sqlite3_trace_v2(
    db: JniPointer,
    mask: Int,
    callback: TraceCallback?
): Int

public external fun sqlite3_txn_state(
    db: JniPointer,
    schema: String?
): Int

public external fun sqlite3_update_hook(
    db: JniPointer,
    callback: UpdateHookCallback?
): UpdateHookCallback?

public external fun sqlite3_uri_boolean(
    fileName: JniPointer,
    parameter: String,
    def: Int
): Int

public external fun sqlite3_uri_int64(
    fileName: JniPointer,
    parameter: String,
    def: Long
): Long

public external fun sqlite3_uri_key(
    fileName: JniPointer,
    index: Int
): String?

public external fun sqlite3_uri_parameter(
    fileName: JniPointer,
    parameter: String
): String?

public external fun sqlite3_user_data(context: JniPointer): Any?

public external fun sqlite3_value_blob(value: JniPointer): ByteArray?

public external fun sqlite3_value_buffer(
    value: JniPointer,
    outSize: OutputPointer.OfInt64
): JniPointer

public external fun sqlite3_value_bytes(value: JniPointer): Int

public external fun sqlite3_value_double(value: JniPointer): Double

public external fun sqlite3_value_dup(value: JniPointer): JniPointer

public external fun sqlite3_value_encoding(value: JniPointer): Int

public external fun sqlite3_value_free(value: JniPointer)

public external fun sqlite3_value_frombind(value: JniPointer): Int

public external fun sqlite3_value_int(value: JniPointer): Int

public external fun sqlite3_value_int64(value: JniPointer): Long

public external fun sqlite3_value_nochange(value: JniPointer): Int

public external fun sqlite3_value_numeric_type(value: JniPointer): Int

public external fun sqlite3_value_pointer(
    value: JniPointer,
    type: String?
): Any?

public external fun sqlite3_value_subtype(value: JniPointer): Int

public external fun sqlite3_value_text(value: JniPointer): String?

public external fun sqlite3_value_type(value: JniPointer): Int

public external fun sqlite3_vfs_find(name: String?): JniPointer

public external fun sqlite3_vfs_register(
    vfs: JniPointer,
    makeDefault: Int
): Int

public external fun sqlite3_vfs_unregister(vfs: JniPointer): Int

public external fun sqlite3_vtab_collation(
    info: JniPointer,
    index: Int,
): String

public external fun sqlite3_vtab_config(
    db: JniPointer,
    option: Int,
    args: Array<*>
): Int

public external fun sqlite3_vtab_distinct(info: JniPointer): Int

public external fun sqlite3_vtab_in(
    info: JniPointer,
    index: Int,
    handle: Int
): Int

public external fun sqlite3_vtab_in_first(
    value: JniPointer,
    outValue: OutputPointer.OfPointer?,
): Int

public external fun sqlite3_vtab_in_next(
    value: JniPointer,
    outValue: OutputPointer.OfPointer?,
): Int

public external fun sqlite3_vtab_nochange(context: JniPointer): Int

public external fun sqlite3_vtab_on_conflict(db: JniPointer): Int

public external fun sqlite3_vtab_rhs_value(
    info: JniPointer,
    index: Int,
    outValue: OutputPointer.OfPointer?,
): Int

public external fun sqlite3_wal_autocheckpoint(
    db: JniPointer,
    nFrame: Int
): Int

public external fun sqlite3_wal_checkpoint(
    db: JniPointer,
    name: String?
): Int

public external fun sqlite3_wal_checkpoint_v2(
    db: JniPointer,
    name: String?,
    mode: Int,
    outNLog: OutputPointer.OfInt32?,
    outNCkpt: OutputPointer.OfInt32?
): Int

public external fun sqlite3_wal_hook(
    db: JniPointer,
    callback: WalHookCallback?
): WalHookCallback?

///////////////////////////////////////////////////////////////////////////
// SQLite Multiple Ciphers
///////////////////////////////////////////////////////////////////////////

public external fun sqlite3mc_cipher_count(): Int

public external fun sqlite3mc_cipher_index(cipherName: String): Int

public external fun sqlite3mc_cipher_name(index: Int): String?

public external fun sqlite3mc_codec_data(
    db: JniPointer,
    schemaName: String?,
    paramName: String
): JniPointer

public external fun sqlite3mc_config(
    db: JniPointer,
    paramName: String,
    newValue: Int
): Int

public external fun sqlite3mc_config_cipher(
    db: JniPointer,
    cipherName: String,
    paramName: String,
    newValue: Int
): Int

public external fun sqlite3mc_register_cipher(
    descriptor: JniPointer,
    params: JniPointer,
    makeDefault: Int
): Int

public external fun sqlite3mc_version(): String

public external fun sqlite3mc_vfs_create(
    realName: String,
    makeDefault: Int
): Int

public external fun sqlite3mc_vfs_destroy(name: String)

public external fun sqlite3mc_vfs_shutdown()

/**
 * Installs a cipher descriptor at the given slot index.
 */
private external fun nativeCipherDescriptorInstall(
    descriptor: JniPointer,
    index: Int,
    callbacks: CipherDescriptorCallbacks<*>
)

internal fun cipherDescriptorInstall(
    descriptor: JniPointer,
    index: Int,
    callbacks: CipherDescriptorCallbacks<*>
) = nativeCipherDescriptorInstall(descriptor, index, callbacks)

/**
 * Uninstall the cipher descriptor at the given slot index.
 */
private external fun nativeCipherDescriptorUninstall(
    descriptor: JniPointer,
    index: Int
)

internal fun cipherDescriptorUninstall(
    descriptor: JniPointer,
    index: Int
) = nativeCipherDescriptorUninstall(descriptor, index)

///////////////////////////////////////////////////////////////////////////
// Virtual Table
///////////////////////////////////////////////////////////////////////////

/**
 * Initializes a virtual table module.
 *
 * The [callbackMask] represents the optional callbacks that are enabled where the LSB is the first
 * callback in the struct (xConnect) and the MSB the last (xIntegrity) in the order they're declared
 * in the struct. Only bits for optional callbacks are taken into account, others are ignored.
 * See [ksqlite.structs.sqlite3_module.Member] for members.
 */
private external fun nativeModuleInit(
    module: JniPointer,
    callbackMask: Int,
    eponymous: Boolean,
    callbacks: VtabModuleCallbacks
)

internal fun moduleInit(
    module: JniPointer,
    callbackMask: Int,
    eponymous: Boolean,
    callbacks: VtabModuleCallbacks
) = nativeModuleInit(module, callbackMask, eponymous, callbacks)

/**
 * Deinitiliazes a virtual table module.
 * This does not deallocates it, only clears associated Java resources.
 */
private external fun nativeModuleDeinit(module: JniPointer)

internal fun moduleDeinit(module: JniPointer) = nativeModuleDeinit(module)

/**
 * Initializes a virtual table.
 */
private external fun nativeVtabInit(vTab: JniPointer)

internal fun vTabInit(vTab: JniPointer) = nativeVtabInit(vTab)

/**
 * Deinitiliazes a virtual table.
 * This does not deallocates it, only clears associated resources.
 */
private external fun nativeVtabDeinit(vTab: JniPointer)

internal fun vTabDeinit(vTab: JniPointer) = nativeVtabDeinit(vTab)

///////////////////////////////////////////////////////////////////////////
// Virtual File System
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes `sqlite3io_methods::[xClose]`.
 */
public external fun ioMethodsClose(
    xClose: JniPointer,
    file: JniPointer
): Int

/**
 * Invokes `sqlite3_vfs::[xOpen]`.
 */
public external fun vfsOpen(
    xOpen: JniPointer,
    vfs: JniPointer,
    name: String?,
    file: JniPointer,
    flags: Int,
    outFlags: OutputPointer.OfInt32?
): Int

/**
 * Invokes `sqlite3_vfs::[xDelete]`.
 */
public external fun vfsDelete(
    xDelete: JniPointer,
    vfs: JniPointer,
    name: String,
    syncDir: Int,
): Int

/**
 * Invokes `sqlite3_vfs::[xAccess]`.
 */
public external fun vfsAccess(
    xAccess: JniPointer,
    vfs: JniPointer,
    name: String,
    flags: Int,
    outFlags: OutputPointer.OfInt32?
): Int