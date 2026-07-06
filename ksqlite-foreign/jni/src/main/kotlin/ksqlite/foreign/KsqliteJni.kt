@file:JvmName("KsqliteJni")
@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.foreign

import ksqlite.foreign.callbacks.AuthorizerCallback
import ksqlite.foreign.callbacks.AutoExtensionCallback
import ksqlite.foreign.callbacks.AutovacuumPagesCallback
import ksqlite.foreign.callbacks.BusyHandlerCallback
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
import ksqlite.foreign.structs.StructType
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
 * Allocates [size] bytes and returns a pointer to the allocated memory.
 * The default allocator is used to obtains memory.
 */
public external fun nativeBufferAllocate(size: Long): Long

/**
 * Frees memory previously obtained using [nativeBufferAllocate].
 */
public external fun nativeBufferFree(pointer: Long)

/**
 * Reads bytes into [destination].
 */
public external fun nativeBufferRead(
    buffer: Long,
    destination: ByteArray,
    size: Int,
    sourceOffset: Long,
    destinationOffset: Int
)

/**
 * Writes bytes from [source].
 */
public external fun nativeBufferWrite(
    buffer: Long,
    source: ByteArray,
    size: Int,
    sourceOffset: Int,
    destinationOffset: Long
)

///////////////////////////////////////////////////////////////////////////
// String helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Reads bytes until null termination marker is found and returns the bytes read as [String].
 */
public external fun nativeReadString(pointer: Long): String

/**
 * Frees [pointer] using `sqlite3_free` ands returns the result of `sqlite3_mprintf` on [message].
 * If [message] is `null` then only [sqlite3_free] is called on [pointer] and `0` is returned
 */
public external fun nativeFreeAndMalloc(
    pointer: Long,
    message: String?
): Long

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the layout of the struct.
 * For a struct member index M:
 *
 * - array[M*2] = offset
 * - array[M*2+1] = length
 *
 * The struct's total size can be obtained by reading the last element of teh returned array.
 *
 * The offset and lentgh are written in the order they're declared in the C-struct.
 */
private external fun nativeStructLayout(type: Int): IntArray

internal fun structLayout(type: StructType): IntArray = nativeStructLayout(type.type)

/**
 * Returns a writable view of the struct as a [ByteBuffer] pointing to [pointer].
 */
private external fun nativeStructReinterpret(
    size: Int,
    pointer: Long
): ByteBuffer

internal fun structReinterpret(
    size: Int,
    pointer: Long
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
// C-API
///////////////////////////////////////////////////////////////////////////

/**
 * To align with other platforms behavior, only one auto extension is allowed.
 */
public external fun ksqlite_auto_extension(callback: AutoExtensionCallback): Int

public external fun ksqlite_cancel_auto_extension(callback: AutoExtensionCallback): Int

public external fun ksqlite_prepare_v2(
    db: Long,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: OutputPointer.OfPointer,
    outOffset: OutputPointer.OfInt32?
): Int

public external fun ksqlite_prepare_v3(
    db: Long,
    sql: ByteArray,
    maxBytes: Int,
    flags: Int,
    outStmt: OutputPointer.OfPointer,
    outOffset: OutputPointer.OfInt32?
): Int

public external fun sqlite3_aggregate_context(
    context: Long,
    create: Boolean
): Long

/**
 * Replaces the previous callback if any.
 */
public external fun sqlite3_autovacuum_pages(
    db: Long,
    callback: AutovacuumPagesCallback?,
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
    bytes: ByteArray,
    size: Int,
    destructor: DestructorCallback?
): Int

public external fun sqlite3_bind_blob64(
    stmt: Long,
    index: Int,
    buffer: Long,
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
    value: String
): Int

public external fun sqlite3_bind_text64(
    stmt: Long,
    index: Int,
    buffer: Long,
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
    rowid: Long,
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
    rowid: Long,
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

public external fun sqlite3_column_buffer(
    stmt: Long,
    index: Int,
    outSize: OutputPointer.OfInt64
): Long

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

public external fun sqlite3_commit_hook(
    db: Long,
    callback: CommitHookCallback?
): CommitHookCallback?

public external fun sqlite3_compileoption_get(index: Int): String?

public external fun sqlite3_compileoption_used(optName: String): Int

public external fun sqlite3_complete(sql: String): Int

public external fun sqlite3_config(
    id: Int,
    args: Array<*>
): Int

public external fun sqlite3_context_db_handle(context: Long): Long

public external fun sqlite3_create_collation_v2(
    db: Long,
    name: String,
    eTextRep: Int,
    destructor: DestructorCallback?,
    callback: CollationCallback?
): Int

public external fun sqlite3_create_function_v2(
    db: Long,
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
    db: Long,
    name: String,
    module: Long,
    appData: Any?,
    destroy: DestructorCallback?,
): Int

public external fun sqlite3_create_window_function(
    db: Long,
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

public external fun sqlite3_data_count(stmt: Long): Int

public external fun sqlite3_db_cacheflush(db: Long): Int

public external fun sqlite3_db_config(
    db: Long,
    option: Int,
    args: Array<*>
): Int

public external fun sqlite3_db_filename(
    db: Long,
    name: String
): Long

public external fun sqlite3_db_handle(stmt: Long): Long

public external fun sqlite3_db_name(
    db: Long,
    index: Int
): String?

public external fun sqlite3_db_readonly(
    db: Long,
    name: String
): Int

public external fun sqlite3_db_release_memory(
    db: Long,
): Int

public external fun sqlite3_db_status(
    db: Long,
    option: Int,
    outCurrent: OutputPointer.OfInt32?,
    outHighwater: OutputPointer.OfInt32?,
    resetFlag: Int,
): Int

public external fun sqlite3_db_status64(
    db: Long,
    option: Int,
    outCurrent: OutputPointer.OfInt64?,
    outHighwater: OutputPointer.OfInt64?,
    resetFlag: Int,
): Int

public external fun sqlite3_declare_vtab(
    db: Long,
    sql: String
): Int

public external fun sqlite3_deserialize(
    db: Long,
    schema: String?,
    buffer: Long,
    dbSize: Long,
    bufferSize: Long,
    flags: Int,
): Int

public external fun sqlite3_drop_modules(
    db: Long,
    modules: Array<String>?
): Int

public external fun sqlite3_errcode(db: Long): Int

public external fun sqlite3_errmsg(db: Long): String?

public external fun sqlite3_error_offset(db: Long): Int

public external fun sqlite3_errstr(resultCode: Int): String?

public external fun sqlite3_exec(
    db: Long,
    sql: String,
    callback: ExecCallback?,
    errorMessage: OutputPointer.OfString?
): Int

public external fun sqlite3_expanded_sql(stmt: Long): String?

public external fun sqlite3_extended_errcode(db: Long): Int

public external fun sqlite3_extended_result_codes(
    db: Long,
    enabled: Int,
): Int

public external fun sqlite3_file_control(
    db: Long,
    name: String?,
    opcode: Int,
    param: Any?
): Int

public external fun sqlite3_filename_database(fileName: Long): String?

public external fun sqlite3_filename_journal(fileName: Long): String?

public external fun sqlite3_filename_wal(fileName: Long): String?

public external fun sqlite3_finalize(stmt: Long): Int

public external fun sqlite3_free(buffer: Long)

public external fun sqlite3_get_autocommit(db: Long): Int

public external fun sqlite3_hard_heap_limit64(limit: Long): Long

public external fun sqlite3_get_auxdata(
    context: Long,
    index: Int,
): Long

public external fun sqlite3_initialize(): Int

public external fun sqlite3_interrupt(db: Long)

public external fun sqlite3_is_interrupted(db: Long): Int

public external fun sqlite3_key(
    db: Long,
    key: ByteArray,
    nKey: Int,
): Int

public external fun sqlite3_key_v2(
    db: Long,
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

public external fun sqlite3_last_insert_rowid(db: Long): Long

public external fun sqlite3_libversion(): String

public external fun sqlite3_libversion_number(): Int

public external fun sqlite3_limit(
    db: Long,
    id: Int,
    newVal: Int
): Int

public external fun sqlite3_log(
    errorCode: Int,
    message: String
)

public external fun sqlite3_malloc(size: Int): Long

public external fun sqlite3_malloc64(size: Long): Long

public external fun sqlite3_memory_used(): Long

public external fun sqlite3_memory_highwater(resetFlag: Int): Long

public external fun sqlite3_msize(buffer: Long): Long

public external fun sqlite3_next_stmt(
    db: Long,
    stmt: Long,
): Long

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
    db: Long,
    name: String,
    nArg: Int
): Int

public external fun sqlite3_prepare_v2(
    db: Long,
    sql: String,
    outStmt: OutputPointer.OfPointer
): Int

public external fun sqlite3_prepare_v3(
    db: Long,
    sql: String,
    flags: Int,
    outStmt: OutputPointer.OfPointer
): Int

public external fun sqlite3_preupdate_blobwrite(db: Long): Int

public external fun sqlite3_preupdate_count(db: Long): Int

public external fun sqlite3_preupdate_depth(db: Long): Int

public external fun sqlite3_preupdate_hook(
    db: Long,
    callback: PreupdateHookCallback?
): PreupdateHookCallback?

public external fun sqlite3_preupdate_new(
    db: Long,
    index: Int,
    outValue: OutputPointer.OfPointer,
): Int

public external fun sqlite3_preupdate_old(
    db: Long,
    index: Int,
    outValue: OutputPointer.OfPointer,
): Int

public external fun sqlite3_progress_handler(
    db: Long,
    nOps: Int,
    callback: ProgressHandlerCallback?
)

public external fun sqlite3_randomness(
    size: Int,
    buffer: Long,
)

public external fun sqlite3_realloc(
    buffer: Long,
    size: Int,
): Long

public external fun sqlite3_realloc64(
    buffer: Long,
    size: Long,
): Long

public external fun sqlite3_rekey(
    db: Long,
    key: ByteArray,
    nKey: Int,
): Int

public external fun sqlite3_rekey_v2(
    db: Long,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Int

public external fun sqlite3_release_memory(size: Int): Int

public external fun sqlite3_reset(stmt: Long): Int

public external fun sqlite3_reset_auto_extension()

public external fun sqlite3_result_blob(
    context: Long,
    bytes: ByteArray,
    size: Int,
    destructor: DestructorCallback?
)

public external fun sqlite3_result_blob64(
    context: Long,
    buffer: Long,
    size: Long,
    destructor: DestructorCallback?
)

public external fun sqlite3_result_double(
    context: Long,
    value: Double,
)

public external fun sqlite3_result_error(
    context: Long,
    message: String
)

public external fun sqlite3_result_error_code(
    context: Long,
    errorCode: Int,
)

public external fun sqlite3_result_error_nomem(context: Long)

public external fun sqlite3_result_error_toobig(context: Long)

public external fun sqlite3_result_int(
    context: Long,
    value: Int,
)

public external fun sqlite3_result_int64(
    context: Long,
    value: Long,
)

public external fun sqlite3_result_null(context: Long)

public external fun sqlite3_result_pointer(
    context: Long,
    data: Any?,
    type: String?,
    destructor: DestructorCallback?
)

public external fun sqlite3_result_subtype(
    context: Long,
    subtype: Int,
)

public external fun sqlite3_result_text(
    context: Long,
    value: String
)

public external fun sqlite3_result_text64(
    context: Long,
    buffer: Long,
    size: Long,
    destructor: DestructorCallback?,
    encoding: Int
)

public external fun sqlite3_result_value(
    context: Long,
    value: Long
)

public external fun sqlite3_result_zeroblob(
    context: Long,
    size: Int,
)

public external fun sqlite3_result_zeroblob64(
    context: Long,
    size: Long,
): Int

public external fun sqlite3_rollback_hook(
    db: Long,
    callback: RollbackHookCallback?
): RollbackHookCallback?

public external fun sqlite3_serialize(
    db: Long,
    schema: String?,
    outSize: OutputPointer.OfInt64,
    flags: Int,
): Long

public external fun sqlite3_set_authorizer(
    db: Long,
    callback: AuthorizerCallback?
): Int

public external fun sqlite3_set_auxdata(
    context: Long,
    index: Int,
    destructor: DestructorCallback?
): Long

public external fun sqlite3_set_errmsg(
    db: Long,
    errorCode: Int,
    message: String?
): Int

public external fun sqlite3_set_last_insert_rowid(
    db: Long,
    rowId: Long,
)

public external fun sqlite3_shutdown(): Int

public external fun sqlite3_snapshot_cmp(
    snapshot1: Long,
    snapshot2: Long,
): Int

public external fun sqlite3_snapshot_free(snapshot: Long)

public external fun sqlite3_snapshot_get(
    db: Long,
    name: String?,
    outSnapshot: OutputPointer.OfPointer
): Int

public external fun sqlite3_snapshot_open(
    db: Long,
    name: String?,
    snapshot: Long
): Int

public external fun sqlite3_snapshot_recover(
    db: Long,
    name: String?
): Int

public external fun sqlite3_soft_heap_limit64(limit: Long): Long

public external fun sqlite3_sourceid(): String

public external fun sqlite3_sql(stmt: Long): String

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

public external fun sqlite3_step(stmt: Long): Int

public external fun sqlite3_stmt_busy(stmt: Long): Int

public external fun sqlite3_stmt_explain(
    stmt: Long,
    mode: Int,
): Int

public external fun sqlite3_stmt_isexplain(stmt: Long): Int

public external fun sqlite3_stmt_readonly(stmt: Long): Int

public external fun sqlite3_stmt_status(
    stmt: Long,
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

public external fun sqlite3_system_errno(db: Long): Int

public external fun sqlite3_table_column_metadata(
    db: Long,
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

public external fun sqlite3_total_changes(db: Long): Int

public external fun sqlite3_total_changes64(db: Long): Long

public external fun sqlite3_trace_v2(
    db: Long,
    mask: Int,
    callback: TraceCallback?
): Int

public external fun sqlite3_txn_state(
    db: Long,
    schema: String?
): Int

public external fun sqlite3_update_hook(
    db: Long,
    callback: UpdateHookCallback?
): UpdateHookCallback?

public external fun sqlite3_uri_boolean(
    fileName: Long,
    parameter: String,
    def: Int
): Int

public external fun sqlite3_uri_int64(
    fileName: Long,
    parameter: String,
    def: Long
): Long

public external fun sqlite3_uri_key(
    fileName: Long,
    index: Int
): String?

public external fun sqlite3_uri_parameter(
    fileName: Long,
    parameter: String
): String?

public external fun sqlite3_user_data(context: Long): Any?

public external fun sqlite3_value_blob(value: Long): ByteArray?

public external fun sqlite3_value_buffer(
    value: Long,
    outSize: OutputPointer.OfInt64
): Long

public external fun sqlite3_value_bytes(value: Long): Int

public external fun sqlite3_value_double(value: Long): Double

public external fun sqlite3_value_dup(value: Long): Long

public external fun sqlite3_value_encoding(value: Long): Int

public external fun sqlite3_value_free(value: Long)

public external fun sqlite3_value_frombind(value: Long): Int

public external fun sqlite3_value_int(value: Long): Int

public external fun sqlite3_value_int64(value: Long): Long

public external fun sqlite3_value_nochange(value: Long): Int

public external fun sqlite3_value_numeric_type(value: Long): Int

public external fun sqlite3_value_pointer(
    value: Long,
    type: String?
): Any?

public external fun sqlite3_value_subtype(value: Long): Int

public external fun sqlite3_value_text(value: Long): String?

public external fun sqlite3_value_type(value: Long): Int

public external fun sqlite3_vfs_find(name: String?): Long

public external fun sqlite3_vfs_register(
    vfs: Long,
    makeDefault: Int
): Int

public external fun sqlite3_vfs_unregister(vfs: Long): Int

public external fun sqlite3_vtab_collation(
    info: Long,
    index: Int,
): String

public external fun sqlite3_vtab_config(
    db: Long,
    option: Int,
    args: Array<*>
): Int

public external fun sqlite3_vtab_distinct(info: Long): Int

public external fun sqlite3_vtab_in(
    info: Long,
    index: Int,
    handle: Int
): Int

public external fun sqlite3_vtab_in_first(
    value: Long,
    outValue: OutputPointer.OfPointer?,
): Int

public external fun sqlite3_vtab_in_next(
    value: Long,
    outValue: OutputPointer.OfPointer?,
): Int

public external fun sqlite3_vtab_nochange(context: Long): Int

public external fun sqlite3_vtab_on_conflict(db: Long): Int

public external fun sqlite3_vtab_rhs_value(
    info: Long,
    index: Int,
    outValue: OutputPointer.OfPointer?,
): Int

public external fun sqlite3_wal_autocheckpoint(
    db: Long,
    nFrame: Int
): Int

public external fun sqlite3_wal_checkpoint(
    db: Long,
    name: String?
): Int

public external fun sqlite3_wal_checkpoint_v2(
    db: Long,
    name: String?,
    mode: Int,
    outNLog: OutputPointer.OfInt32?,
    outNCkpt: OutputPointer.OfInt32?
): Int

public external fun sqlite3_wal_hook(
    db: Long,
    callback: WalHookCallback?
): WalHookCallback?

///////////////////////////////////////////////////////////////////////////
// Virtual Table
///////////////////////////////////////////////////////////////////////////

/**
 * Initializes a virtual table module.
 *
 * The [callbackMask] represents the optional callbacks that are enabled where the LSB is the first
 * callback in the struct (xConnect) and the MSB the last (xIntegrity) in the order they're declared
 * in the struct. Only bits for optional callbacks are taken into account, others are ignored.
 * See [ksqlite.foreign.structs.sqlite3_module.Layout] for per-bit symbols.
 */
private external fun nativeModuleInit(
    module: Long,
    callbackMask: Int,
    eponymous: Boolean,
    callbacks: VtabModuleCallbacks
)

internal fun moduleInit(
    module: Long,
    callbackMask: Int,
    eponymous: Boolean,
    callbacks: VtabModuleCallbacks
) = nativeModuleInit(module, callbackMask, eponymous, callbacks)

/**
 * Deinitiliazes a virtual table module.
 * This does not deallocates it, only clears associated Java resources.
 */
private external fun nativeModuleDeinit(module: Long)

internal fun moduleDeinit(module: Long) = nativeModuleDeinit(module)

/**
 * Initializes a virtual table.
 */
private external fun nativeVtabInit(vTab: Long)

internal fun vTabInit(vTab: Long) = nativeVtabInit(vTab)

/**
 * Deinitiliazes a virtual table.
 * This does not deallocates it, only clears associated resources.
 */
private external fun nativeVtabDeinit(vTab: Long)

internal fun vTabDeinit(vTab: Long) = nativeVtabDeinit(vTab)

///////////////////////////////////////////////////////////////////////////
// Virtual File System
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes `sqlite3io_methods::[xClose]`.
 */
public external fun ioMethodsClose(
    xClose: Long,
    file: Long
): Int

/**
 * Invokes `sqlite3_vfs::[xOpen]`.
 */
public external fun vfsOpen(
    xOpen: Long,
    vfs: Long,
    name: String?,
    file: Long,
    flags: Int,
    outFlags: OutputPointer.OfInt32?
): Int

/**
 * Invokes `sqlite3_vfs::[xDelete]`.
 */
public external fun vfsDelete(
    xDelete: Long,
    vfs: Long,
    name: String,
    syncDir: Int,
): Int

/**
 * Invokes `sqlite3_vfs::[xAccess]`.
 */
public external fun vfsAccess(
    xAccess: Long,
    vfs: Long,
    name: String,
    flags: Int,
    outFlags: OutputPointer.OfInt32?
): Int