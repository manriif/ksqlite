@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCStringArray
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.SQLITE_TRANSIENT
import ksqlite.capi.callbacks.Sqlite3AuthorizerCallback
import ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback
import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback
import ksqlite.capi.callbacks.Sqlite3CollationCompareCallback
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.callbacks.Sqlite3ExecCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFinalCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFuncCallback
import ksqlite.capi.callbacks.Sqlite3FunctionInverseCallback
import ksqlite.capi.callbacks.Sqlite3FunctionStepCallback
import ksqlite.capi.callbacks.Sqlite3FunctionValueCallback
import ksqlite.capi.callbacks.Sqlite3PreupdateHookCallback
import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback
import ksqlite.capi.callbacks.Sqlite3TraceCallback
import ksqlite.capi.callbacks.Sqlite3UpdateHookCallback
import ksqlite.capi.callbacks.Sqlite3WalHookCallback
import ksqlite.capi.handlers.AuthorizerHandler
import ksqlite.capi.handlers.AutoVacuumPagesHandler
import ksqlite.capi.handlers.BusyHandlerHandler
import ksqlite.capi.handlers.CollationCompareHandler
import ksqlite.capi.handlers.CollationNeededHandler
import ksqlite.capi.handlers.CommitHookHandler
import ksqlite.capi.handlers.ConfigLogHandler
import ksqlite.capi.handlers.ConfigSqlLogHandler
import ksqlite.capi.handlers.ExecHandler
import ksqlite.capi.handlers.FunctionFinalHandler
import ksqlite.capi.handlers.FunctionFuncHandler
import ksqlite.capi.handlers.FunctionInverseHandler
import ksqlite.capi.handlers.FunctionStepHandler
import ksqlite.capi.handlers.FunctionValueHandler
import ksqlite.capi.handlers.PreupdateHookHandler
import ksqlite.capi.handlers.ProgressHandlerHandler
import ksqlite.capi.handlers.RollbackHookHandler
import ksqlite.capi.handlers.TraceHandler
import ksqlite.capi.handlers.UpdateHookHandler
import ksqlite.capi.handlers.WalHookHandler
import ksqlite.capi.handlers.callbackHandler
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.bufferDisposer
import ksqlite.capi.memory.copyBytes
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.Int64OutputParam
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobOutputParam
import ksqlite.capi.types.Sqlite3CheckpointMode
import ksqlite.capi.types.Sqlite3CompleteResult
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3DbStatusOption
import ksqlite.capi.types.Sqlite3DeserializeFlag
import ksqlite.capi.types.Sqlite3ExplainMode
import ksqlite.capi.types.Sqlite3FileControlOpcode
import ksqlite.capi.types.Sqlite3Limit
import ksqlite.capi.types.Sqlite3OpenFlag
import ksqlite.capi.types.Sqlite3OutputParam
import ksqlite.capi.types.Sqlite3PrepareFlag
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3SerializeFlag
import ksqlite.capi.types.Sqlite3SnapshotOutputParam
import ksqlite.capi.types.Sqlite3StatementStatusCounter
import ksqlite.capi.types.Sqlite3StatusOption
import ksqlite.capi.types.Sqlite3StmtOutputParam
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3TraceCode
import ksqlite.capi.types.Sqlite3TransactionState
import ksqlite.capi.types.Sqlite3ValueOutputParam
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_filename
import ksqlite.capi.types.sqlite3_snapshot
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.sqlite3_vfs
import ksqlite.capi.types.useParam
import ksqlite.capi.types.useParamMemScoped
import ksqlite.capi.types.useParams
import ksqlite.capi.types.useParamsMemScoped
import ksqlite.capi.vtab.Sqlite3VTabConfigOption
import ksqlite.capi.vtab.createVTabModule
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.ksqlite_auto_extension
import ksqlite.ksqlite_cancel_auto_extension
import ksqlite.ksqlite_prepare_v2
import ksqlite.ksqlite_prepare_v3
import ksqlite.sqlite3_autovacuum_pages as native_sqlite3_autovacuum_pages
import ksqlite.sqlite3_backup_finish as native_sqlite3_backup_finish
import ksqlite.sqlite3_backup_init as native_sqlite3_backup_init
import ksqlite.sqlite3_backup_pagecount as native_sqlite3_backup_pagecount
import ksqlite.sqlite3_backup_remaining as native_sqlite3_backup_remaining
import ksqlite.sqlite3_backup_step as native_sqlite3_backup_step
import ksqlite.sqlite3_bind_blob as native_sqlite3_bind_blob
import ksqlite.sqlite3_bind_blob64 as native_sqlite3_bind_blob64
import ksqlite.sqlite3_bind_double as native_sqlite3_bind_double
import ksqlite.sqlite3_bind_int as native_sqlite3_bind_int
import ksqlite.sqlite3_bind_int64 as native_sqlite3_bind_int64
import ksqlite.sqlite3_bind_null as native_sqlite3_bind_null
import ksqlite.sqlite3_bind_parameter_count as native_sqlite3_bind_parameter_count
import ksqlite.sqlite3_bind_parameter_index as native_sqlite3_bind_parameter_index
import ksqlite.sqlite3_bind_parameter_name as native_sqlite3_bind_parameter_name
import ksqlite.sqlite3_bind_pointer as native_sqlite3_bind_pointer
import ksqlite.sqlite3_bind_text as native_sqlite3_bind_text
import ksqlite.sqlite3_bind_text64 as native_sqlite3_bind_text64
import ksqlite.sqlite3_bind_value as native_sqlite3_bind_value
import ksqlite.sqlite3_bind_zeroblob as native_sqlite3_bind_zeroblob
import ksqlite.sqlite3_bind_zeroblob64 as native_sqlite3_bind_zeroblob64
import ksqlite.sqlite3_blob_bytes as native_sqlite3_blob_bytes
import ksqlite.sqlite3_blob_close as native_sqlite3_blob_close
import ksqlite.sqlite3_blob_open as native_sqlite3_blob_open
import ksqlite.sqlite3_blob_read as native_sqlite3_blob_read
import ksqlite.sqlite3_blob_reopen as native_sqlite3_blob_reopen
import ksqlite.sqlite3_blob_write as native_sqlite3_blob_write
import ksqlite.sqlite3_busy_handler as native_sqlite3_busy_handler
import ksqlite.sqlite3_busy_timeout as native_sqlite3_busy_timeout
import ksqlite.sqlite3_changes as native_sqlite3_changes
import ksqlite.sqlite3_changes64 as native_sqlite3_changes64
import ksqlite.sqlite3_clear_bindings as native_sqlite3_clear_bindings
import ksqlite.sqlite3_close as native_sqlite3_close
import ksqlite.sqlite3_close_v2 as native_sqlite3_close_v2
import ksqlite.sqlite3_collation_needed as native_sqlite3_collation_needed
import ksqlite.sqlite3_column_blob as native_sqlite3_column_blob
import ksqlite.sqlite3_column_bytes as native_sqlite3_column_bytes
import ksqlite.sqlite3_column_count as native_sqlite3_column_count
import ksqlite.sqlite3_column_database_name as native_sqlite3_column_database_name
import ksqlite.sqlite3_column_decltype as native_sqlite3_column_decltype
import ksqlite.sqlite3_column_double as native_sqlite3_column_double
import ksqlite.sqlite3_column_int as native_sqlite3_column_int
import ksqlite.sqlite3_column_int64 as native_sqlite3_column_int64
import ksqlite.sqlite3_column_name as native_sqlite3_column_name
import ksqlite.sqlite3_column_origin_name as native_sqlite3_column_origin_name
import ksqlite.sqlite3_column_table_name as native_sqlite3_column_table_name
import ksqlite.sqlite3_column_text as native_sqlite3_column_text
import ksqlite.sqlite3_column_type as native_sqlite3_column_type
import ksqlite.sqlite3_column_value as native_sqlite3_column_value
import ksqlite.sqlite3_commit_hook as native_sqlite3_commit_hook
import ksqlite.sqlite3_compileoption_get as native_sqlite3_compileoption_get
import ksqlite.sqlite3_compileoption_used as native_sqlite3_compileoption_used
import ksqlite.sqlite3_complete as native_sqlite3_complete
import ksqlite.sqlite3_config as native_sqlite3_config
import ksqlite.sqlite3_context_db_handle as native_sqlite3_context_db_handle
import ksqlite.sqlite3_create_collation_v2 as native_sqlite3_create_collation_v2
import ksqlite.sqlite3_create_function_v2 as native_sqlite3_create_function_v2
import ksqlite.sqlite3_create_module_v2 as native_sqlite3_create_module_v2
import ksqlite.sqlite3_create_window_function as native_sqlite3_create_window_function
import ksqlite.sqlite3_data_count as native_sqlite3_data_count
import ksqlite.sqlite3_db_cacheflush as native_sqlite3_db_cacheflush
import ksqlite.sqlite3_db_config as native_sqlite3_db_config
import ksqlite.sqlite3_db_filename as native_sqlite3_db_filename
import ksqlite.sqlite3_db_handle as native_sqlite3_db_handle
import ksqlite.sqlite3_db_name as native_sqlite3_db_name
import ksqlite.sqlite3_db_readonly as native_sqlite3_db_readonly
import ksqlite.sqlite3_db_release_memory as native_sqlite3_db_release_memory
import ksqlite.sqlite3_db_status as native_sqlite3_db_status
import ksqlite.sqlite3_db_status64 as native_sqlite3_db_status64
import ksqlite.sqlite3_declare_vtab as native_sqlite3_declare_vtab
import ksqlite.sqlite3_deserialize as native_sqlite3_deserialize
import ksqlite.sqlite3_drop_modules as native_sqlite3_drop_modules
import ksqlite.sqlite3_errcode as native_sqlite3_errcode
import ksqlite.sqlite3_errmsg as native_sqlite3_errmsg
import ksqlite.sqlite3_error_offset as native_sqlite3_error_offset
import ksqlite.sqlite3_errstr as native_sqlite3_errstr
import ksqlite.sqlite3_exec as native_sqlite3_exec
import ksqlite.sqlite3_expanded_sql as native_sqlite3_expanded_sql
import ksqlite.sqlite3_extended_errcode as native_sqlite3_extended_errcode
import ksqlite.sqlite3_extended_result_codes as native_sqlite3_extended_result_codes
import ksqlite.sqlite3_file_control as native_sqlite3_file_control
import ksqlite.sqlite3_finalize as native_sqlite3_finalize
import ksqlite.sqlite3_free as native_sqlite3_free
import ksqlite.sqlite3_get_autocommit as native_sqlite3_get_autocommit
import ksqlite.sqlite3_hard_heap_limit64 as native_sqlite3_hard_heap_limit64
import ksqlite.sqlite3_initialize as native_sqlite3_initialize
import ksqlite.sqlite3_interrupt as native_sqlite3_interrupt
import ksqlite.sqlite3_is_interrupted as native_sqlite3_is_interrupted
import ksqlite.sqlite3_key as native_sqlite3_key
import ksqlite.sqlite3_key_v2 as native_sqlite3_key_v2
import ksqlite.sqlite3_keyword_check as native_sqlite3_keyword_check
import ksqlite.sqlite3_keyword_count as native_sqlite3_keyword_count
import ksqlite.sqlite3_keyword_name as native_sqlite3_keyword_name
import ksqlite.sqlite3_last_insert_rowid as native_sqlite3_last_insert_rowid
import ksqlite.sqlite3_libversion as native_sqlite3_libversion
import ksqlite.sqlite3_libversion_number as native_sqlite3_libversion_number
import ksqlite.sqlite3_limit as native_sqlite3_limit
import ksqlite.sqlite3_log as native_sqlite3_log
import ksqlite.sqlite3_malloc as native_sqlite3_malloc
import ksqlite.sqlite3_malloc64 as native_sqlite3_malloc64
import ksqlite.sqlite3_memory_highwater as native_sqlite3_memory_highwater
import ksqlite.sqlite3_memory_used as native_sqlite3_memory_used
import ksqlite.sqlite3_msize as native_sqlite3_msize
import ksqlite.sqlite3_next_stmt as native_sqlite3_next_stmt
import ksqlite.sqlite3_open as native_sqlite3_open
import ksqlite.sqlite3_open_v2 as native_sqlite3_open_v2
import ksqlite.sqlite3_overload_function as native_sqlite3_overload_function
import ksqlite.sqlite3_prepare_v2 as native_sqlite3_prepare_v2
import ksqlite.sqlite3_prepare_v3 as native_sqlite3_prepare_v3
import ksqlite.sqlite3_preupdate_blobwrite as native_sqlite3_preupdate_blobwrite
import ksqlite.sqlite3_preupdate_count as native_sqlite3_preupdate_count
import ksqlite.sqlite3_preupdate_depth as native_sqlite3_preupdate_depth
import ksqlite.sqlite3_preupdate_hook as native_sqlite3_preupdate_hook
import ksqlite.sqlite3_preupdate_new as native_sqlite3_preupdate_new
import ksqlite.sqlite3_preupdate_old as native_sqlite3_preupdate_old
import ksqlite.sqlite3_progress_handler as native_sqlite3_progress_handler
import ksqlite.sqlite3_randomness as native_sqlite3_randomness
import ksqlite.sqlite3_realloc as native_sqlite3_realloc
import ksqlite.sqlite3_realloc64 as native_sqlite3_realloc64
import ksqlite.sqlite3_rekey as native_sqlite3_rekey
import ksqlite.sqlite3_rekey_v2 as native_sqlite3_rekey_v2
import ksqlite.sqlite3_release_memory as native_sqlite3_release_memory
import ksqlite.sqlite3_reset as native_sqlite3_reset
import ksqlite.sqlite3_reset_auto_extension as native_sqlite3_reset_auto_extension
import ksqlite.sqlite3_result_blob as native_sqlite3_result_blob
import ksqlite.sqlite3_result_blob64 as native_sqlite3_result_blob64
import ksqlite.sqlite3_result_double as native_sqlite3_result_double
import ksqlite.sqlite3_result_error as native_sqlite3_result_error
import ksqlite.sqlite3_result_error_code as native_sqlite3_result_error_code
import ksqlite.sqlite3_result_error_nomem as native_sqlite3_result_error_nomem
import ksqlite.sqlite3_result_error_toobig as native_sqlite3_result_error_toobig
import ksqlite.sqlite3_result_int as native_sqlite3_result_int
import ksqlite.sqlite3_result_int64 as native_sqlite3_result_int64
import ksqlite.sqlite3_result_null as native_sqlite3_result_null
import ksqlite.sqlite3_result_pointer as native_sqlite3_result_pointer
import ksqlite.sqlite3_result_subtype as native_sqlite3_result_subtype
import ksqlite.sqlite3_result_text as native_sqlite3_result_text
import ksqlite.sqlite3_result_text64 as native_sqlite3_result_text64
import ksqlite.sqlite3_result_value as native_sqlite3_result_value
import ksqlite.sqlite3_result_zeroblob as native_sqlite3_result_zeroblob
import ksqlite.sqlite3_result_zeroblob64 as native_sqlite3_result_zeroblob64
import ksqlite.sqlite3_rollback_hook as native_sqlite3_rollback_hook
import ksqlite.sqlite3_serialize as native_sqlite3_serialize
import ksqlite.sqlite3_set_authorizer as native_sqlite3_set_authorizer
import ksqlite.sqlite3_set_errmsg as native_sqlite3_set_errmsg
import ksqlite.sqlite3_set_last_insert_rowid as native_sqlite3_set_last_insert_rowid
import ksqlite.sqlite3_shutdown as native_sqlite3_shutdown
import ksqlite.sqlite3_snapshot_cmp as native_sqlite3_snapshot_cmp
import ksqlite.sqlite3_snapshot_free as native_sqlite3_snapshot_free
import ksqlite.sqlite3_snapshot_get as native_sqlite3_snapshot_get
import ksqlite.sqlite3_snapshot_open as native_sqlite3_snapshot_open
import ksqlite.sqlite3_snapshot_recover as native_sqlite3_snapshot_recover
import ksqlite.sqlite3_soft_heap_limit64 as native_sqlite3_soft_heap_limit64
import ksqlite.sqlite3_sourceid as native_sqlite3_sourceid
import ksqlite.sqlite3_sql as native_sqlite3_sql
import ksqlite.sqlite3_status as native_sqlite3_status
import ksqlite.sqlite3_status64 as native_sqlite3_status64
import ksqlite.sqlite3_step as native_sqlite3_step
import ksqlite.sqlite3_stmt_busy as native_sqlite3_stmt_busy
import ksqlite.sqlite3_stmt_explain as native_sqlite3_stmt_explain
import ksqlite.sqlite3_stmt_isexplain as native_sqlite3_stmt_isexplain
import ksqlite.sqlite3_stmt_readonly as native_sqlite3_stmt_readonly
import ksqlite.sqlite3_stmt_status as native_sqlite3_stmt_status
import ksqlite.sqlite3_strglob as native_sqlite3_strglob
import ksqlite.sqlite3_stricmp as native_sqlite3_stricmp
import ksqlite.sqlite3_strlike as native_sqlite3_strlike
import ksqlite.sqlite3_strnicmp as native_sqlite3_strnicmp
import ksqlite.sqlite3_system_errno as native_sqlite3_system_errno
import ksqlite.sqlite3_table_column_metadata as native_sqlite3_table_column_metadata
import ksqlite.sqlite3_total_changes as native_sqlite3_total_changes
import ksqlite.sqlite3_total_changes64 as native_sqlite3_total_changes64
import ksqlite.sqlite3_trace_v2 as native_sqlite3_trace_v2
import ksqlite.sqlite3_txn_state as native_sqlite3_txn_state
import ksqlite.sqlite3_update_hook as native_sqlite3_update_hook
import ksqlite.sqlite3_uri_boolean as native_sqlite3_uri_boolean
import ksqlite.sqlite3_uri_int64 as native_sqlite3_uri_int64
import ksqlite.sqlite3_uri_key as native_sqlite3_uri_key
import ksqlite.sqlite3_uri_parameter as native_sqlite3_uri_parameter
import ksqlite.sqlite3_value_blob as native_sqlite3_value_blob
import ksqlite.sqlite3_value_bytes as native_sqlite3_value_bytes
import ksqlite.sqlite3_value_double as native_sqlite3_value_double
import ksqlite.sqlite3_value_dup as native_sqlite3_value_dup
import ksqlite.sqlite3_value_encoding as native_sqlite3_value_encoding
import ksqlite.sqlite3_value_free as native_sqlite3_value_free
import ksqlite.sqlite3_value_frombind as native_sqlite3_value_frombind
import ksqlite.sqlite3_value_int as native_sqlite3_value_int
import ksqlite.sqlite3_value_int64 as native_sqlite3_value_int64
import ksqlite.sqlite3_value_nochange as native_sqlite3_value_nochange
import ksqlite.sqlite3_value_numeric_type as native_sqlite3_value_numeric_type
import ksqlite.sqlite3_value_subtype as native_sqlite3_value_subtype
import ksqlite.sqlite3_value_text as native_sqlite3_value_text
import ksqlite.sqlite3_value_type as native_sqlite3_value_type
import ksqlite.sqlite3_vfs_find as native_sqlite3_vfs_find
import ksqlite.sqlite3_vfs_register as native_sqlite3_vfs_register
import ksqlite.sqlite3_vfs_unregister as native_sqlite3_vfs_unregister
import ksqlite.sqlite3_vtab_collation as native_sqlite3_vtab_collation
import ksqlite.sqlite3_vtab_config as native_sqlite3_vtab_config
import ksqlite.sqlite3_vtab_distinct as native_sqlite3_vtab_distinct
import ksqlite.sqlite3_vtab_in as native_sqlite3_vtab_in
import ksqlite.sqlite3_vtab_in_first as native_sqlite3_vtab_in_first
import ksqlite.sqlite3_vtab_in_next as native_sqlite3_vtab_in_next
import ksqlite.sqlite3_vtab_nochange as native_sqlite3_vtab_nochange
import ksqlite.sqlite3_vtab_on_conflict as native_sqlite3_vtab_on_conflict
import ksqlite.sqlite3_vtab_rhs_value as native_sqlite3_vtab_rhs_value
import ksqlite.sqlite3_wal_autocheckpoint as native_sqlite3_wal_autocheckpoint
import ksqlite.sqlite3_wal_checkpoint as native_sqlite3_wal_checkpoint
import ksqlite.sqlite3_wal_checkpoint_v2 as native_sqlite3_wal_checkpoint_v2
import ksqlite.sqlite3_wal_hook as native_sqlite3_wal_hook

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the raw values of `this` [VariadicValue] array.
 *
 * TODO: due to Kotlin interop limitation, spreading on the returned array is not supported so all
 *  the aruments must be passed in the form of `the_function(args[0], args[1], ...)`.
 */
private fun Array<out VariadicValue<COpaquePointer>?>.toVariadicArguments(
    manager: () -> MemoryManager
): Array<Any?> {
    return map { value ->
        when (value) {
            is OfString -> manager().keyedStringPointer(value.key, value.value)
            else -> value?.value
        }
    }.toTypedArray()
}

/**
 * Throws an exception due to unhadled variadic argument.
 */
private fun variadicArgumentsError(): Nothing {
    throw IllegalStateException("Unexpected number of arguments for variadic function call")
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

public actual fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result =
    autoExtensionRegister(callback) { ksqlite_auto_extension(AutoExtensionHandler) }

public actual fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<in AppData>?,
    callback: Sqlite3AutoVacuumPagesCallback<in AppData>?
): Sqlite3Result = convertResult(
    native_sqlite3_autovacuum_pages(
        db.pointer,
        callbackHandler(callback, AutoVacuumPagesHandler),
        db.memory.keyedStableRefPointer(KEY_AUTOVACUUM_PAGES, callback, appData, destroy),
        stableRefDisposer(callback, destroy)
    )
)

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result =
    convertResult(native_sqlite3_backup_finish(backup.pointer))

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = native_sqlite3_backup_init(
    destDb.pointer,
    destDbName,
    srcDb.pointer,
    srcDbName
)?.let(::sqlite3_backup)

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    native_sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    native_sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result = convertResult(native_sqlite3_backup_step(backup.pointer, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
): Sqlite3Result = convertResult(
    native_sqlite3_bind_blob(
        stmt.pointer,
        index,
        stmt.memory.byteArrayPointer(bytes, destroy),
        size,
        globalDisposer(bytes)
    )
)

public actual fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result = convertResult(
    native_sqlite3_bind_blob64(
        stmt.pointer,
        index,
        buffer.pointer,
        size.convert(),
        bufferDisposer(buffer, destroy)
    )
)

public actual fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Sqlite3Result = convertResult(native_sqlite3_bind_double(stmt.pointer, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result = convertResult(native_sqlite3_bind_int(stmt.pointer, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result = convertResult(native_sqlite3_bind_int64(stmt.pointer, index, value))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result = convertResult(native_sqlite3_bind_null(stmt.pointer, index))

public actual fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int =
    native_sqlite3_bind_parameter_count(stmt.pointer)

public actual fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int = native_sqlite3_bind_parameter_index(stmt.pointer, name)

public actual fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_bind_parameter_name(stmt.pointer, index)
    ?.toKStringFromUtf8()

public actual fun <Data> sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Data,
    type: String?,
    destroy: Sqlite3DestroyCallback<Data>?
): Sqlite3Result = convertResult(allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
    native_sqlite3_bind_pointer(
        stmt.pointer,
        index,
        stmt.memory.stableRefPointer(ptr, data, ptrDestroy),
        ptr.name,
        stableRefDisposer(ptr, ptrDestroy)
    )
})

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    value: String
): Sqlite3Result = convertResult(memScoped {
    val cText = value.cstr
    native_sqlite3_bind_text(stmt.pointer, index, cText.ptr, cText.size, SQLITE_TRANSIENT)
})

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result = convertResult(
    native_sqlite3_bind_text64(
        stmt.pointer,
        index,
        buffer.pointer,
        size.convert(),
        bufferDisposer(buffer, destroy),
        encoding.utf8OrThrow().value.convert()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(native_sqlite3_bind_value(stmt.pointer, index, value.pointer))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result = convertResult(native_sqlite3_bind_zeroblob(stmt.pointer, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result = convertResult(native_sqlite3_bind_zeroblob64(stmt.pointer, index, size))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    native_sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result =
    convertResult(native_sqlite3_blob_close(blob.pointer))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Sqlite3BlobOpenFlag,
    outBlob: Sqlite3BlobOutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outBlob) { blobPtr ->
        native_sqlite3_blob_open(
            db.pointer,
            databaseName.cstr.ptr,
            tableName.cstr.ptr,
            columnName.cstr.ptr,
            rowIndex,
            flags.value,
            blobPtr
        )
    }
})

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    bytes: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result =
    convertResult(native_sqlite3_blob_read(blob.pointer, bytes.refTo(0), size, offset))

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result = convertResult(native_sqlite3_blob_reopen(blob.pointer, rowIndex))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    bytes: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result =
    convertResult(native_sqlite3_blob_write(blob.pointer, bytes.refTo(0), size, offset))

public actual fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3BusyHandlerCallback<AppData>?
): Sqlite3Result = convertResult(
    native_sqlite3_busy_handler(
        db.pointer,
        callbackHandler(callback, BusyHandlerHandler),
        db.memory.keyedStableRefPointer(KEY_BUSY_HANDLER, callback, appData)
    )
)

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result = convertResult(native_sqlite3_busy_timeout(db.pointer, millis))

public actual fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int =
    autoExtensionUnregister(callback) { ksqlite_cancel_auto_extension(AutoExtensionHandler) }

public actual fun sqlite3_changes(db: sqlite3): Int =
    native_sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    native_sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result =
    commonClearBindings(stmt, native_sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3): Sqlite3Result =
    db.deallocate { native_sqlite3_close(it.pointer) }

public actual fun sqlite3_close_v2(db: sqlite3): Sqlite3Result =
    db.deallocate { native_sqlite3_close_v2(it.pointer) }

public actual fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CollationNeededCallback<AppData>?,
): Sqlite3Result = convertResult(
    native_sqlite3_collation_needed(
        db.pointer,
        db.memory.keyedStableRefPointer(KEY_COLLATION_NEEDED, callback, appData),
        callbackHandler(callback, CollationNeededHandler)
    )
)

public actual fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): ByteArray? = commonColumnByteArray(
    stmt = stmt,
    index = index,
    pointer = native_sqlite3_column_blob(stmt.pointer, index),
    toByteArray = COpaquePointer::copyBytes
)

public actual fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int = native_sqlite3_column_bytes(stmt.pointer, index)

public actual fun sqlite3_column_count(stmt: sqlite3_stmt): Int =
    native_sqlite3_column_count(stmt.pointer)

public actual fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_database_name(stmt.pointer, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_decltype(stmt.pointer, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double = native_sqlite3_column_double(stmt.pointer, index)

public actual fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int = native_sqlite3_column_int(stmt.pointer, index)

public actual fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long = native_sqlite3_column_int64(stmt.pointer, index)

public actual fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_name(stmt.pointer, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_origin_name(stmt.pointer, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_table_name(stmt.pointer, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_text(stmt.pointer, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3DataType = convertDataType(native_sqlite3_column_type(stmt.pointer, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = native_sqlite3_column_value(stmt.pointer, index)
    ?.let(::sqlite3_value)

public actual fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CommitHookCallback<AppData>?
) {
    native_sqlite3_commit_hook(
        db.pointer,
        callbackHandler(callback, CommitHookHandler),
        db.memory.keyedStableRefPointer(KEY_COMMIT_HOOK, callback, appData)
    )
}

public actual fun sqlite3_compileoption_get(index: Int): String? =
    native_sqlite3_compileoption_get(index)?.toKStringFromUtf8()

public actual fun sqlite3_compileoption_used(optName: String): Int =
    native_sqlite3_compileoption_used(optName)

public actual fun sqlite3_complete(sql: String): Sqlite3CompleteResult =
    convertCompleteResult(native_sqlite3_complete(sql))

public actual fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result = commonConfig(
    option = option,
    logFunctionPointer = { cb, _ -> callbackHandler(cb, ConfigLogHandler) },
    sqllogFunctionPointer = { cb, _ -> callbackHandler(cb, ConfigSqlLogHandler) },
    bufferPointer = Buffer::pointer,
    keyedStableRefPointer = globalMemory::keyedStableRefPointer,
    rowidInView = {
        useParamMemScoped(param) { paramPtr ->
            native_sqlite3_config(id, paramPtr)
        }
    },
    nativeConfig = { id, values ->
        val args = values.toVariadicArguments(::globalMemory)

        when (args.size) {
            0 -> native_sqlite3_config(id)
            1 -> native_sqlite3_config(id, args[0])
            2 -> native_sqlite3_config(id, args[0], args[1])
            3 -> native_sqlite3_config(id, args[0], args[1], args[2])
            else -> variadicArgumentsError()
        }
    }
)

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3 =
    sqlite3(native_sqlite3_context_db_handle(context.pointer)!!)

public actual fun <AppData> sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<in AppData>?,
    callback: Sqlite3CollationCompareCallback<in AppData>?
): Sqlite3Result = convertResult(
    native_sqlite3_create_collation_v2(
        db.pointer,
        name,
        encoding.utf8OrThrow().value,
        db.memory.keyedStableRefPointer(collationKey(name, encoding), callback, appData, destroy),
        callbackHandler(callback, CollationCompareHandler),
        stableRefDisposer(callback, destroy)
    )
)

public actual fun <AppData> sqlite3_create_function_v2(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    appData: AppData,
    func: Sqlite3FunctionFuncCallback<in AppData>?,
    step: Sqlite3FunctionStepCallback<in AppData>?,
    final: Sqlite3FunctionFinalCallback<in AppData>?,
    destroy: Sqlite3DestroyCallback<in AppData>?
): Sqlite3Result = convertResult(
    createFunction(appData, func, step, final, destroy) { fn, fnDestroy ->
        native_sqlite3_create_function_v2(
            db.pointer,
            name,
            nArg,
            encoding.utf8OrThrow().value,
            db.memory.keyedStableRefPointer(
                key = functionKey(name, nArg, encoding),
                data = fn,
                appData = appData,
                destructor = fnDestroy
            ),
            callbackHandler(func, FunctionFuncHandler),
            callbackHandler(step, FunctionStepHandler),
            callbackHandler(final, FunctionFinalHandler),
            stableRefDisposer(fn, destroy)
        )
    }
)

public actual fun <AppData> sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module<AppData>?,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?
): Sqlite3Result = convertResult(createVTabModule(module?.callbacks, appData) { vTabModule ->
    native_sqlite3_create_module_v2(
        db.pointer,
        name,
        module?.pointer,
        db.memory.keyedStableRefPointer(moduleKey(name), vTabModule, appData, destroy),
        stableRefDisposer(vTabModule, destroy)
    )
})

public actual fun <AppData> sqlite3_create_window_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    appData: AppData,
    step: Sqlite3FunctionStepCallback<in AppData>?,
    final: Sqlite3FunctionFinalCallback<in AppData>?,
    value: Sqlite3FunctionValueCallback<in AppData>?,
    inverse: Sqlite3FunctionInverseCallback<in AppData>?,
    destroy: Sqlite3DestroyCallback<in AppData>?
): Sqlite3Result = convertResult(
    createWindowFunction(appData, step, final, value, inverse, destroy) { fn, fnDestroy ->
        native_sqlite3_create_window_function(
            db.pointer,
            name,
            nArg,
            encoding.utf8OrThrow().value,
            db.memory.keyedStableRefPointer(
                key = windowFunctionKey(name, nArg, encoding),
                data = fn,
                appData = appData,
                destructor = fnDestroy
            ),
            callbackHandler(step, FunctionStepHandler),
            callbackHandler(final, FunctionFinalHandler),
            callbackHandler(value, FunctionValueHandler),
            callbackHandler(inverse, FunctionInverseHandler),
            stableRefDisposer(fn, destroy)
        )
    }
)

public actual fun sqlite3_data_count(stmt: sqlite3_stmt): Int =
    native_sqlite3_data_count(stmt.pointer)

public actual fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result =
    convertResult(native_sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: Sqlite3DbConfigOption,
): Sqlite3Result = commonDbConfig(
    option = option,
    bufferPointer = Buffer::pointer,
    outParamConfig = {
        useParamMemScoped(state) { statePtr ->
            native_sqlite3_db_config(db.pointer, id, value, statePtr)
        }
    },
    nativeConfig = { id, values ->
        val args = values.toVariadicArguments(db::memory)

        when (args.size) {
            0 -> native_sqlite3_db_config(db.pointer, id)
            1 -> native_sqlite3_db_config(db.pointer, id, args[0])
            2 -> native_sqlite3_db_config(db.pointer, id, args[0], args[1])
            3 -> native_sqlite3_db_config(db.pointer, id, args[0], args[1], args[2])
            else -> variadicArgumentsError()
        }
    }
)

public actual fun sqlite3_db_filename(
    db: sqlite3,
    name: String
): sqlite3_filename? = native_sqlite3_db_filename(db.pointer, name)
    ?.toKStringFromUtf8()

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    native_sqlite3_db_handle(stmt.pointer)?.let(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = native_sqlite3_db_name(db.pointer, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    name: String
): Int = native_sqlite3_db_readonly(db.pointer, name)

public actual fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result =
    convertResult(native_sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native_sqlite3_db_status(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native_sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result = convertResult(native_sqlite3_declare_vtab(db.pointer, sql))

public actual fun sqlite3_deserialize(
    db: sqlite3,
    schema: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: Sqlite3DeserializeFlag?
): Sqlite3Result = convertResult(
    native_sqlite3_deserialize(
        db.pointer,
        schema,
        buffer.pointer.reinterpret(),
        dbSize,
        bufferSize,
        flags?.value?.convert() ?: 0u
    )
)

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result = convertResult(memScoped {
    native_sqlite3_drop_modules(db.pointer, keep?.toCStringArray(this))
})

public actual fun sqlite3_errcode(db: sqlite3): Int =
    native_sqlite3_errcode(db.pointer)

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    native_sqlite3_errmsg(db.pointer)?.toKStringFromUtf8()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    native_sqlite3_error_offset(db.pointer)

public actual fun sqlite3_errstr(resultCode: Int): String? =
    native_sqlite3_errstr(resultCode)?.toKStringFromUtf8()

public actual fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: Sqlite3ExecCallback<AppData>?
): Sqlite3Result = convertResult(useMemoryManager {
    memScoped {
        useParam(outErrorMessage) { errorMessagePtr ->
            native_sqlite3_exec(
                db.pointer,
                sql.cstr.ptr,
                callbackHandler(callback, ExecHandler),
                stableRefPointer(callback, appData),
                errorMessagePtr
            )
        }
    }
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = native_sqlite3_expanded_sql(stmt.pointer) ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    native_sqlite3_free(pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): Int =
    native_sqlite3_extended_errcode(db.pointer)

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result = convertResult(native_sqlite3_extended_result_codes(db.pointer, enabled))

public actual fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode
): Sqlite3Result = convertResult(
    native_sqlite3_file_control(db.pointer, name, opcode.code, null)
)

public actual fun sqlite3_finalize(stmt: sqlite3_stmt): Sqlite3Result =
    stmt.deallocate { native_sqlite3_finalize(stmt.pointer) }

public actual fun sqlite3_free(buffer: Buffer): Unit =
    native_sqlite3_free(buffer.pointer)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    native_sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    native_sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): Sqlite3Result =
    convertResult(native_sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    native_sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    native_sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(native_sqlite3_key(db.pointer, key.refTo(0), nKey))

public actual fun sqlite3_key_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result =
    convertResult(native_sqlite3_key_v2(db.pointer, dbName, key.refTo(0), nKey))

public actual fun sqlite3_keyword_check(word: String): Int = memScoped {
    val cWord = word.cstr
    native_sqlite3_keyword_check(cWord, cWord.size)
}

public actual fun sqlite3_keyword_count(): Int =
    native_sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): Sqlite3Result = convertResult(memScoped {
    useParam(outName) { namePtr ->
        val size = Int32OutputParam(0)

        useParam(size) { sizePtr ->
            native_sqlite3_keyword_name(index, namePtr, sizePtr)
        }.also {
            outName.size = size.value
        }
    }
})

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    native_sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    native_sqlite3_libversion()!!.toKStringFromUtf8()

public actual fun sqlite3_libversion_number(db: sqlite3): Int =
    native_sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: Sqlite3Limit,
    newVal: Int
): Int = native_sqlite3_limit(db.pointer, id.id, newVal)

public actual fun sqlite3_log(
    errorCode: Int,
    message: String
): Unit = native_sqlite3_log(errorCode, message)

public actual fun sqlite3_malloc(size: Int): Buffer? =
    native_sqlite3_malloc(size)?.let { Buffer.from(it, size.toLong()) }

public actual fun sqlite3_malloc64(size: Long): Buffer? =
    native_sqlite3_malloc64(size.toULong())?.let { Buffer.from(it, size) }

public actual fun sqlite3_memory_used(): Long =
    native_sqlite3_memory_used()

public actual fun sqlite3_memory_highwater(resetFlag: Int): Long =
    native_sqlite3_memory_highwater(resetFlag)

public actual fun sqlite3_msize(buffer: Buffer): ULong =
    native_sqlite3_msize(buffer.pointer)

public actual fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt
): sqlite3_stmt? = native_sqlite3_next_stmt(db.pointer, stmt.pointer)
    ?.let(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: Sqlite3OutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPtr ->
        native_sqlite3_open(fileName.cstr.ptr, dbPtr)
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: Sqlite3OutputParam,
    flags: Sqlite3OpenFlag.Db,
    vfs: String?
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPtr ->
        native_sqlite3_open_v2(fileName.cstr.ptr, dbPtr, flags.value, vfs?.cstr?.ptr)
    }
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): Sqlite3Result = convertResult(native_sqlite3_overload_function(db.pointer, name, nArg))

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result = convertResult(useParamsMemScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
    ksqlite_prepare_v2(db.pointer, sql.refTo(0), maxBytes, stmtPtr, offsetPtr)
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.cstr
        native_sqlite3_prepare_v2(db.pointer, cSql.ptr, cSql.size, stmtPtr, null)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result = convertResult(useParamsMemScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
    val prepFlags = flags?.value?.convert() ?: 0u
    ksqlite_prepare_v3(db.pointer, sql.refTo(0), maxBytes, prepFlags, stmtPtr, offsetPtr)
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outStmt) { stmtPtr ->
        val csql = sql.cstr
        val prepFlags = flags?.value?.convert() ?: 0u
        native_sqlite3_prepare_v3(db.pointer, csql.ptr, csql.size, prepFlags, stmtPtr, null)
    }
})

public actual fun sqlite3_preupdate_blobwrite(db: sqlite3): Int =
    native_sqlite3_preupdate_blobwrite(db.pointer)

public actual fun sqlite3_preupdate_count(db: sqlite3): Int =
    native_sqlite3_preupdate_count(db.pointer)

public actual fun sqlite3_preupdate_depth(db: sqlite3): Int =
    native_sqlite3_preupdate_depth(db.pointer)

public actual fun <AppData> sqlite3_preupdate_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3PreupdateHookCallback<AppData>?
) {
    native_sqlite3_preupdate_hook(
        db.pointer,
        callbackHandler(callback, PreupdateHookHandler),
        db.memory.keyedStableRefPointer(KEY_PREUPDATE_HOOK, callback, appData)
    )
}

public actual fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_preupdate_new(db.pointer, index, valuePtr)
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_preupdate_old(db.pointer, index, valuePtr)
})

public actual fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: Sqlite3ProgressHandlerCallback<AppData>?
): Unit = native_sqlite3_progress_handler(
    db.pointer,
    nOps,
    callbackHandler(callback, ProgressHandlerHandler),
    db.memory.keyedStableRefPointer(KEY_PROGRESS_HANDLER, callback, appData)
)

public actual fun sqlite3_randomness(
    size: Int,
    buffer: Buffer
): Unit = native_sqlite3_randomness(size, buffer.pointer)

public actual fun sqlite3_realloc(
    buffer: Buffer,
    size: Int
): Buffer? = Buffer.from(
    pointer = native_sqlite3_realloc(buffer.pointer, size),
    size = size.toLong()
)

public actual fun sqlite3_realloc64(
    buffer: Buffer,
    size: Long
): Buffer? = Buffer.from(
    pointer = native_sqlite3_realloc64(buffer.pointer, size.convert()),
    size = size
)

public actual fun sqlite3_rekey(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(native_sqlite3_rekey(db.pointer, key.refTo(0), nKey))

public actual fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result =
    convertResult(native_sqlite3_rekey_v2(db.pointer, dbName, key.refTo(0), nKey))

public actual fun sqlite3_release_memory(size: Int): Int =
    native_sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native_sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { native_sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
): Unit = native_sqlite3_result_blob(
    context.pointer,
    context.db.memory.byteArrayPointer(bytes, destroy),
    size,
    globalDisposer(bytes)
)

public actual fun sqlite3_result_blob64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Unit = native_sqlite3_result_blob64(
    context.pointer,
    buffer.pointer,
    size.convert(),
    bufferDisposer(buffer, destroy)
)

public actual fun sqlite3_result_double(
    context: sqlite3_context,
    value: Double
): Unit = native_sqlite3_result_double(context.pointer, value)

public actual fun sqlite3_result_error(
    context: sqlite3_context,
    message: String
): Unit = memScoped {
    val cMessage = message.cstr
    native_sqlite3_result_error(context.pointer, cMessage.ptr, cMessage.size)
}

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    result: Sqlite3Result.Failure
): Unit = native_sqlite3_result_error_code(context.pointer, result.code)

public actual fun sqlite3_result_error_nomem(context: sqlite3_context): Unit =
    native_sqlite3_result_error_nomem(context.pointer)

public actual fun sqlite3_result_error_toobig(context: sqlite3_context): Unit =
    native_sqlite3_result_error_toobig(context.pointer)

public actual fun sqlite3_result_int(
    context: sqlite3_context,
    value: Int
): Unit = native_sqlite3_result_int(context.pointer, value)

public actual fun sqlite3_result_int64(
    context: sqlite3_context,
    value: Long
): Unit = native_sqlite3_result_int64(context.pointer, value)

public actual fun sqlite3_result_null(context: sqlite3_context): Unit =
    native_sqlite3_result_null(context.pointer)

public actual fun <Data> sqlite3_result_pointer(
    context: sqlite3_context,
    data: Data,
    type: String?,
    destroy: Sqlite3DestroyCallback<Data>?
): Unit = allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
    native_sqlite3_result_pointer(
        context.pointer,
        context.db.memory.stableRefPointer(ptr, data, ptrDestroy),
        ptr.name,
        stableRefDisposer(ptr, ptrDestroy)
    )
}

public actual fun sqlite3_result_subtype(
    context: sqlite3_context,
    subtype: UInt
): Unit = native_sqlite3_result_subtype(context.pointer, subtype)

public actual fun sqlite3_result_text(
    context: sqlite3_context,
    value: String
): Unit = memScoped {
    val cText = value.cstr
    native_sqlite3_result_text(context.pointer, cText.ptr, cText.size, SQLITE_TRANSIENT)
}

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Unit = native_sqlite3_result_text64(
    context.pointer,
    buffer.pointer,
    size.convert(),
    bufferDisposer(buffer, destroy),
    encoding.utf8OrThrow().value.convert()
)

public actual fun sqlite3_result_value(
    context: sqlite3_context,
    value: sqlite3_value,
): Unit = native_sqlite3_result_value(context.pointer, value.pointer)

public actual fun sqlite3_result_zeroblob(
    context: sqlite3_context,
    size: Int
): Unit = native_sqlite3_result_zeroblob(context.pointer, size)

public actual fun sqlite3_result_zeroblob64(
    context: sqlite3_context,
    size: ULong
): Sqlite3Result = convertResult(native_sqlite3_result_zeroblob64(context.pointer, size))

public actual fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3RollbackHookCallback<AppData>?
) {
    native_sqlite3_rollback_hook(
        db.pointer,
        callbackHandler(callback, RollbackHookHandler),
        db.memory.keyedStableRefPointer(KEY_ROLLBACK_HOOK, callback, appData)
    )
}

public actual fun sqlite3_serialize(
    db: sqlite3,
    schema: String?,
    flags: Sqlite3SerializeFlag?
): Buffer? {
    val size = Int64OutputParam(0)

    val pointer = memScoped {
        useParam(size) { sizePtr ->
            val mFlags = flags?.value?.convert() ?: 0u
            native_sqlite3_serialize(db.pointer, schema?.cstr?.ptr, sizePtr, mFlags)
        }
    }

    return Buffer.from(pointer, size.value)
}

public actual fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3AuthorizerCallback<AppData>?
): Sqlite3Result = convertResult(
    native_sqlite3_set_authorizer(
        db.pointer,
        callbackHandler(callback, AuthorizerHandler),
        db.memory.keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, appData)
    )
)

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: Sqlite3Result.Failure,
    message: String?
): Sqlite3Result = convertResult(native_sqlite3_set_errmsg(db.pointer, errorCode.code, message))

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = native_sqlite3_set_last_insert_rowid(db.pointer, rowId)

public actual fun sqlite3_shutdown(): Sqlite3Result =
    convertResult(native_sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = native_sqlite3_snapshot_cmp(snapshot1.pointer, snapshot2.pointer)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    native_sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: Sqlite3SnapshotOutputParam
): Sqlite3Result = convertResult(useParamMemScoped(outSnapshot) { snapshotPtr ->
    native_sqlite3_snapshot_get(db.pointer, name, snapshotPtr)
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): Sqlite3Result = convertResult(native_sqlite3_snapshot_open(db.pointer, name, snapshot.pointer))

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(native_sqlite3_snapshot_recover(db.pointer, name))

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    native_sqlite3_soft_heap_limit64(limit)

public actual fun sqlite3_sourceid(): String =
    native_sqlite3_sourceid()!!.toKStringFromUtf8()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    native_sqlite3_sql(stmt.pointer)!!.toKStringFromUtf8()

public actual fun sqlite3_status(
    option: Sqlite3StatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native_sqlite3_status(option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_status64(
    option: Sqlite3StatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native_sqlite3_status64(option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native_sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    native_sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: Sqlite3ExplainMode
): Sqlite3Result = convertResult(native_sqlite3_stmt_explain(stmt.pointer, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode =
    convertExplainMode(native_sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    native_sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: Sqlite3StatementStatusCounter,
    resetFlag: Int
): Int = native_sqlite3_stmt_status(stmt.pointer, counter.id, resetFlag)

public actual fun sqlite3_strglob(
    globPattern: String,
    input: String
): Int = native_sqlite3_strglob(globPattern, input)

public actual fun sqlite3_stricmp(
    first: String,
    second: String
): Int = native_sqlite3_stricmp(first, second)

public actual fun sqlite3_strlike(
    likePattern: String,
    input: String,
    escapeCharacter: Char
): Int = native_sqlite3_strlike(likePattern, input, escapeCharacter.code.convert())

public actual fun sqlite3_strnicmp(
    first: String,
    second: String,
    maxCharacters: Int
): Int = native_sqlite3_strnicmp(first, second, maxCharacters)

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    native_sqlite3_system_errno(db.pointer)

public actual fun sqlite3_table_column_metadata(
    db: sqlite3,
    dbName: String?,
    tableName: String,
    columnName: String,
    outDataType: Utf8OutputParam?,
    outCollationName: Utf8OutputParam?,
    outNotNull: Int32OutputParam?,
    outPrimaryKey: Int32OutputParam?,
    outAutoIncrement: Int32OutputParam?
): Sqlite3Result = convertResult(memScoped {
    val dataTypePtr = outDataType?.attach(this)
    val collationNamePtr = outCollationName?.attach(this)
    val notNullPtr = outNotNull?.attach(this)
    val primaryKeyPtr = outPrimaryKey?.attach(this)
    val autoIncrementPtr = outAutoIncrement?.attach(this)

    try {
        native_sqlite3_table_column_metadata(
            db.pointer,
            dbName?.cstr?.ptr,
            tableName.cstr.ptr,
            columnName.cstr.ptr,
            dataTypePtr,
            collationNamePtr,
            notNullPtr,
            primaryKeyPtr,
            autoIncrementPtr
        )
    } finally {
        dataTypePtr?.let(outDataType::detach)
        collationNamePtr?.let(outCollationName::detach)
        notNullPtr?.let(outNotNull::detach)
        primaryKeyPtr?.let(outPrimaryKey::detach)
        autoIncrementPtr?.let(outAutoIncrement::detach)
    }
})

public actual fun sqlite3_total_changes(db: sqlite3): Int =
    native_sqlite3_total_changes(db.pointer)

public actual fun sqlite3_total_changes64(db: sqlite3): Long =
    native_sqlite3_total_changes64(db.pointer)

public actual fun <AppData> sqlite3_trace_v2(
    db: sqlite3,
    mask: Sqlite3TraceCode?,
    appData: AppData,
    callback: Sqlite3TraceCallback<AppData>?
): Sqlite3Result = convertResult(
    native_sqlite3_trace_v2(
        db.pointer,
        mask?.value?.convert() ?: 0U,
        callbackHandler(callback, TraceHandler),
        db.memory.keyedStableRefPointer(KEY_TRACE, callback, appData)
    )
)

public actual fun sqlite3_txn_state(
    db: sqlite3,
    schema: String?
): Sqlite3TransactionState? = convertTransactionState(native_sqlite3_txn_state(db.pointer, schema))

public actual fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3UpdateHookCallback<AppData>?
) {
    native_sqlite3_update_hook(
        db.pointer,
        callbackHandler(callback, UpdateHookHandler),
        db.memory.keyedStableRefPointer(KEY_UPDATE_HOOK, callback, appData)
    )
}

public actual fun sqlite3_uri_boolean(
    fileName: sqlite3_filename,
    parameter: String,
    default: Int
): Int = native_sqlite3_uri_boolean(fileName, parameter, default)

public actual fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long = native_sqlite3_uri_int64(fileName, parameter, default)

public actual fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String? = native_sqlite3_uri_key(fileName, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String? = native_sqlite3_uri_parameter(fileName, parameter)
    ?.toKStringFromUtf8()

public actual fun sqlite3_value_blob(value: sqlite3_value): ByteArray? = commonValueByteArray(
    value = value,
    pointer = native_sqlite3_value_blob(value.pointer),
    toByteArray = COpaquePointer::copyBytes
)

public actual fun sqlite3_value_bytes(value: sqlite3_value): Int =
    native_sqlite3_value_bytes(value.pointer)

public actual fun sqlite3_value_double(value: sqlite3_value): Double =
    native_sqlite3_value_double(value.pointer)

public actual fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value? =
    native_sqlite3_value_dup(value.pointer)
        ?.let(::sqlite3_value)

public actual fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2 =
    convertTextEncoding(native_sqlite3_value_encoding(value.pointer))

public actual fun sqlite3_value_free(value: sqlite3_value): Unit =
    native_sqlite3_value_free(value.pointer)

public actual fun sqlite3_value_frombind(value: sqlite3_value): Int =
    native_sqlite3_value_frombind(value.pointer)

public actual fun sqlite3_value_int(value: sqlite3_value): Int =
    native_sqlite3_value_int(value.pointer)

public actual fun sqlite3_value_int64(value: sqlite3_value): Long =
    native_sqlite3_value_int64(value.pointer)

public actual fun sqlite3_value_nochange(value: sqlite3_value): Int =
    native_sqlite3_value_nochange(value.pointer)

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(native_sqlite3_value_numeric_type(value.pointer))

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    native_sqlite3_value_subtype(value.pointer)

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    native_sqlite3_value_text(value.pointer)?.toKStringFromUtf8()

public actual fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(native_sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? =
    native_sqlite3_vfs_find(name)?.let(::sqlite3_vfs)

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): Sqlite3Result = convertResult(
    native_sqlite3_vfs_register(vfs.pointer, makeDefault)
)

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): Sqlite3Result =
    convertResult(native_sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String? = native_sqlite3_vtab_collation(info.pointer, index)
    ?.toKStringFromUtf8()

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: Sqlite3VTabConfigOption
): Sqlite3Result = commonVtabConfig(option) { id, values ->
    val args = values.toVariadicArguments(db::memory)

    when (args.size) {
        0 -> native_sqlite3_vtab_config(db.pointer, id)
        1 -> native_sqlite3_vtab_config(db.pointer, id, args[0])
        else -> variadicArgumentsError()
    }
}

public actual fun sqlite3_vtab_distinct(info: sqlite3_index_info): Int =
    native_sqlite3_vtab_distinct(info.pointer)

public actual fun sqlite3_vtab_in(
    info: sqlite3_index_info,
    index: Int,
    handle: Int
): Int = native_sqlite3_vtab_in(info.pointer, index, handle)


public actual fun sqlite3_vtab_in_first(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_in_first(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_in_next(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    native_sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): Sqlite3Result =
    convertResult(native_sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_rhs_value(info.pointer, index, valuePtr)
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): Sqlite3Result = convertResult(native_sqlite3_wal_autocheckpoint(db.pointer, nFrame))

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(native_sqlite3_wal_checkpoint(db.pointer, name))

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    name: String?,
    mode: Sqlite3CheckpointMode,
    outNLog: Int32OutputParam?,
    outNCkpt: Int32OutputParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
        native_sqlite3_wal_checkpoint_v2(db.pointer, name?.cstr?.ptr, mode.id, nLogPtr, nCkptPtr)
    }
})

public actual fun <AppData> sqlite3_wal_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3WalHookCallback<AppData>?
) {
    native_sqlite3_wal_hook(
        db.pointer,
        callbackHandler(callback, WalHookHandler),
        db.memory.keyedStableRefPointer(KEY_WAL_HOOK, callback, appData)
    )
}