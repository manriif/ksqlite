@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

//import ksqlite.sqlite3_create_module_v2 as jni_sqlite3_create_module_v2
//import ksqlite.sqlite3_drop_modules as jni_sqlite3_drop_modules
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
import ksqlite.capi.handlers.SharedAutoExtensionHandler
import ksqlite.capi.handlers.TraceHandler
import ksqlite.capi.handlers.UpdateHookHandler
import ksqlite.capi.handlers.callbackHandler
import ksqlite.capi.handlers.destructorHandler
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.deallocateNullable
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.wrapOrNull
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.Int64OutputParam
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobOutputParam
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
import ksqlite.capi.types.useParam
import ksqlite.capi.types.useParams
import ksqlite.ksqliteLoadLibrary
import ksqlite.ksqlite_cancel_auto_extension
import ksqlite.ksqlite_prepare_v2
import ksqlite.ksqlite_prepare_v3
import ksqlite.ksqlite_auto_extension as jni_ksqlite_auto_extension
import ksqlite.sqlite3_autovacuum_pages as jni_sqlite3_autovacuum_pages
import ksqlite.sqlite3_backup_finish as jni_sqlite3_backup_finish
import ksqlite.sqlite3_backup_init as jni_sqlite3_backup_init
import ksqlite.sqlite3_backup_pagecount as jni_sqlite3_backup_pagecount
import ksqlite.sqlite3_backup_remaining as jni_sqlite3_backup_remaining
import ksqlite.sqlite3_backup_step as jni_sqlite3_backup_step
import ksqlite.sqlite3_bind_blob as jni_sqlite3_bind_blob
import ksqlite.sqlite3_bind_blob64 as jni_sqlite3_bind_blob64
import ksqlite.sqlite3_bind_double as jni_sqlite3_bind_double
import ksqlite.sqlite3_bind_int as jni_sqlite3_bind_int
import ksqlite.sqlite3_bind_int64 as jni_sqlite3_bind_int64
import ksqlite.sqlite3_bind_null as jni_sqlite3_bind_null
import ksqlite.sqlite3_bind_parameter_count as jni_sqlite3_bind_parameter_count
import ksqlite.sqlite3_bind_parameter_index as jni_sqlite3_bind_parameter_index
import ksqlite.sqlite3_bind_parameter_name as jni_sqlite3_bind_parameter_name
import ksqlite.sqlite3_bind_pointer as jni_sqlite3_bind_pointer
import ksqlite.sqlite3_bind_text as jni_sqlite3_bind_text
import ksqlite.sqlite3_bind_text64 as jni_sqlite3_bind_text64
import ksqlite.sqlite3_bind_value as jni_sqlite3_bind_value
import ksqlite.sqlite3_bind_zeroblob as jni_sqlite3_bind_zeroblob
import ksqlite.sqlite3_bind_zeroblob64 as jni_sqlite3_bind_zeroblob64
import ksqlite.sqlite3_blob_bytes as jni_sqlite3_blob_bytes
import ksqlite.sqlite3_blob_close as jni_sqlite3_blob_close
import ksqlite.sqlite3_blob_open as jni_sqlite3_blob_open
import ksqlite.sqlite3_blob_read as jni_sqlite3_blob_read
import ksqlite.sqlite3_blob_reopen as jni_sqlite3_blob_reopen
import ksqlite.sqlite3_blob_write as jni_sqlite3_blob_write
import ksqlite.sqlite3_busy_handler as jni_sqlite3_busy_handler
import ksqlite.sqlite3_busy_timeout as jni_sqlite3_busy_timeout
import ksqlite.sqlite3_changes as jni_sqlite3_changes
import ksqlite.sqlite3_changes64 as jni_sqlite3_changes64
import ksqlite.sqlite3_clear_bindings as jni_sqlite3_clear_bindings
import ksqlite.sqlite3_close as jni_sqlite3_close
import ksqlite.sqlite3_close_v2 as jni_sqlite3_close_v2
import ksqlite.sqlite3_collation_needed as jni_sqlite3_collation_needed
import ksqlite.sqlite3_column_blob as jni_sqlite3_column_blob
import ksqlite.sqlite3_column_bytes as jni_sqlite3_column_bytes
import ksqlite.sqlite3_column_count as jni_sqlite3_column_count
import ksqlite.sqlite3_column_database_name as jni_sqlite3_column_database_name
import ksqlite.sqlite3_column_decltype as jni_sqlite3_column_decltype
import ksqlite.sqlite3_column_double as jni_sqlite3_column_double
import ksqlite.sqlite3_column_int as jni_sqlite3_column_int
import ksqlite.sqlite3_column_int64 as jni_sqlite3_column_int64
import ksqlite.sqlite3_column_name as jni_sqlite3_column_name
import ksqlite.sqlite3_column_origin_name as jni_sqlite3_column_origin_name
import ksqlite.sqlite3_column_table_name as jni_sqlite3_column_table_name
import ksqlite.sqlite3_column_text as jni_sqlite3_column_text
import ksqlite.sqlite3_column_type as jni_sqlite3_column_type
import ksqlite.sqlite3_column_value as jni_sqlite3_column_value
import ksqlite.sqlite3_commit_hook as jni_sqlite3_commit_hook
import ksqlite.sqlite3_compileoption_get as jni_sqlite3_compileoption_get
import ksqlite.sqlite3_compileoption_used as jni_sqlite3_compileoption_used
import ksqlite.sqlite3_complete as jni_sqlite3_complete
import ksqlite.sqlite3_config as jni_sqlite3_config
import ksqlite.sqlite3_context_db_handle as jni_sqlite3_context_db_handle
import ksqlite.sqlite3_create_collation_v2 as jni_sqlite3_create_collation_v2
import ksqlite.sqlite3_create_function_v2 as jni_sqlite3_create_function_v2
import ksqlite.sqlite3_create_window_function as jni_sqlite3_create_window_function
import ksqlite.sqlite3_data_count as jni_sqlite3_data_count
import ksqlite.sqlite3_db_cacheflush as jni_sqlite3_db_cacheflush
import ksqlite.sqlite3_db_config as jni_sqlite3_db_config
import ksqlite.sqlite3_db_filename as jni_sqlite3_db_filename
import ksqlite.sqlite3_db_handle as jni_sqlite3_db_handle
import ksqlite.sqlite3_db_name as jni_sqlite3_db_name
import ksqlite.sqlite3_db_readonly as jni_sqlite3_db_readonly
import ksqlite.sqlite3_db_release_memory as jni_sqlite3_db_release_memory
import ksqlite.sqlite3_db_status as jni_sqlite3_db_status
import ksqlite.sqlite3_db_status64 as jni_sqlite3_db_status64
import ksqlite.sqlite3_declare_vtab as jni_sqlite3_declare_vtab
import ksqlite.sqlite3_deserialize as jni_sqlite3_deserialize
import ksqlite.sqlite3_errcode as jni_sqlite3_errcode
import ksqlite.sqlite3_errmsg as jni_sqlite3_errmsg
import ksqlite.sqlite3_error_offset as jni_sqlite3_error_offset
import ksqlite.sqlite3_errstr as jni_sqlite3_errstr
import ksqlite.sqlite3_exec as jni_sqlite3_exec
import ksqlite.sqlite3_expanded_sql as jni_sqlite3_expanded_sql
import ksqlite.sqlite3_extended_errcode as jni_sqlite3_extended_errcode
import ksqlite.sqlite3_extended_result_codes as jni_sqlite3_extended_result_codes
import ksqlite.sqlite3_file_control as jni_sqlite3_file_control
import ksqlite.sqlite3_finalize as jni_sqlite3_finalize
import ksqlite.sqlite3_free as jni_sqlite3_free
import ksqlite.sqlite3_get_autocommit as jni_sqlite3_get_autocommit
import ksqlite.sqlite3_hard_heap_limit64 as jni_sqlite3_hard_heap_limit64
import ksqlite.sqlite3_initialize as jni_sqlite3_initialize
import ksqlite.sqlite3_interrupt as jni_sqlite3_interrupt
import ksqlite.sqlite3_is_interrupted as jni_sqlite3_is_interrupted
import ksqlite.sqlite3_key as jni_sqlite3_key
import ksqlite.sqlite3_key_v2 as jni_sqlite3_key_v2
import ksqlite.sqlite3_keyword_check as jni_sqlite3_keyword_check
import ksqlite.sqlite3_keyword_count as jni_sqlite3_keyword_count
import ksqlite.sqlite3_keyword_name as jni_sqlite3_keyword_name
import ksqlite.sqlite3_last_insert_rowid as jni_sqlite3_last_insert_rowid
import ksqlite.sqlite3_libversion as jni_sqlite3_libversion
import ksqlite.sqlite3_libversion_number as jni_sqlite3_libversion_number
import ksqlite.sqlite3_limit as jni_sqlite3_limit
import ksqlite.sqlite3_log as jni_sqlite3_log
import ksqlite.sqlite3_malloc as jni_sqlite3_malloc
import ksqlite.sqlite3_malloc64 as jni_sqlite3_malloc64
import ksqlite.sqlite3_memory_highwater as jni_sqlite3_memory_highwater
import ksqlite.sqlite3_memory_used as jni_sqlite3_memory_used
import ksqlite.sqlite3_msize as jni_sqlite3_msize
import ksqlite.sqlite3_next_stmt as jni_sqlite3_next_stmt
import ksqlite.sqlite3_open as jni_sqlite3_open
import ksqlite.sqlite3_open_v2 as jni_sqlite3_open_v2
import ksqlite.sqlite3_overload_function as jni_sqlite3_overload_function
import ksqlite.sqlite3_prepare_v2 as jni_sqlite3_prepare_v2
import ksqlite.sqlite3_prepare_v3 as jni_sqlite3_prepare_v3
import ksqlite.sqlite3_preupdate_blobwrite as jni_sqlite3_preupdate_blobwrite
import ksqlite.sqlite3_preupdate_count as jni_sqlite3_preupdate_count
import ksqlite.sqlite3_preupdate_depth as jni_sqlite3_preupdate_depth
import ksqlite.sqlite3_preupdate_hook as jni_sqlite3_preupdate_hook
import ksqlite.sqlite3_preupdate_new as jni_sqlite3_preupdate_new
import ksqlite.sqlite3_preupdate_old as jni_sqlite3_preupdate_old
import ksqlite.sqlite3_progress_handler as jni_sqlite3_progress_handler
import ksqlite.sqlite3_randomness as jni_sqlite3_randomness
import ksqlite.sqlite3_realloc as jni_sqlite3_realloc
import ksqlite.sqlite3_realloc64 as jni_sqlite3_realloc64
import ksqlite.sqlite3_rekey as jni_sqlite3_rekey
import ksqlite.sqlite3_rekey_v2 as jni_sqlite3_rekey_v2
import ksqlite.sqlite3_release_memory as jni_sqlite3_release_memory
import ksqlite.sqlite3_reset as jni_sqlite3_reset
import ksqlite.sqlite3_reset_auto_extension as jni_sqlite3_reset_auto_extension
import ksqlite.sqlite3_result_blob as jni_sqlite3_result_blob
import ksqlite.sqlite3_result_blob64 as jni_sqlite3_result_blob64
import ksqlite.sqlite3_result_double as jni_sqlite3_result_double
import ksqlite.sqlite3_result_error as jni_sqlite3_result_error
import ksqlite.sqlite3_result_error_code as jni_sqlite3_result_error_code
import ksqlite.sqlite3_result_error_nomem as jni_sqlite3_result_error_nomem
import ksqlite.sqlite3_result_error_toobig as jni_sqlite3_result_error_toobig
import ksqlite.sqlite3_result_int as jni_sqlite3_result_int
import ksqlite.sqlite3_result_int64 as jni_sqlite3_result_int64
import ksqlite.sqlite3_result_null as jni_sqlite3_result_null
import ksqlite.sqlite3_result_pointer as jni_sqlite3_result_pointer
import ksqlite.sqlite3_result_subtype as jni_sqlite3_result_subtype
import ksqlite.sqlite3_result_text as jni_sqlite3_result_text
import ksqlite.sqlite3_result_text64 as jni_sqlite3_result_text64
import ksqlite.sqlite3_result_value as jni_sqlite3_result_value
import ksqlite.sqlite3_result_zeroblob as jni_sqlite3_result_zeroblob
import ksqlite.sqlite3_result_zeroblob64 as jni_sqlite3_result_zeroblob64
import ksqlite.sqlite3_rollback_hook as jni_sqlite3_rollback_hook
import ksqlite.sqlite3_serialize as jni_sqlite3_serialize
import ksqlite.sqlite3_set_authorizer as jni_sqlite3_set_authorizer
import ksqlite.sqlite3_set_errmsg as jni_sqlite3_set_errmsg
import ksqlite.sqlite3_set_last_insert_rowid as jni_sqlite3_set_last_insert_rowid
import ksqlite.sqlite3_shutdown as jni_sqlite3_shutdown
import ksqlite.sqlite3_snapshot_cmp as jni_sqlite3_snapshot_cmp
import ksqlite.sqlite3_snapshot_free as jni_sqlite3_snapshot_free
import ksqlite.sqlite3_snapshot_get as jni_sqlite3_snapshot_get
import ksqlite.sqlite3_snapshot_open as jni_sqlite3_snapshot_open
import ksqlite.sqlite3_snapshot_recover as jni_sqlite3_snapshot_recover
import ksqlite.sqlite3_soft_heap_limit64 as jni_sqlite3_soft_heap_limit64
import ksqlite.sqlite3_sourceid as jni_sqlite3_sourceid
import ksqlite.sqlite3_sql as jni_sqlite3_sql
import ksqlite.sqlite3_status as jni_sqlite3_status
import ksqlite.sqlite3_status64 as jni_sqlite3_status64
import ksqlite.sqlite3_step as jni_sqlite3_step
import ksqlite.sqlite3_stmt_busy as jni_sqlite3_stmt_busy
import ksqlite.sqlite3_stmt_explain as jni_sqlite3_stmt_explain
import ksqlite.sqlite3_stmt_isexplain as jni_sqlite3_stmt_isexplain
import ksqlite.sqlite3_stmt_readonly as jni_sqlite3_stmt_readonly
import ksqlite.sqlite3_stmt_status as jni_sqlite3_stmt_status
import ksqlite.sqlite3_strglob as jni_sqlite3_strglob
import ksqlite.sqlite3_stricmp as jni_sqlite3_stricmp
import ksqlite.sqlite3_strlike as jni_sqlite3_strlike
import ksqlite.sqlite3_strnicmp as jni_sqlite3_strnicmp
import ksqlite.sqlite3_system_errno as jni_sqlite3_system_errno
import ksqlite.sqlite3_table_column_metadata as jni_sqlite3_table_column_metadata
import ksqlite.sqlite3_total_changes as jni_sqlite3_total_changes
import ksqlite.sqlite3_total_changes64 as jni_sqlite3_total_changes64
import ksqlite.sqlite3_trace_v2 as jni_sqlite3_trace_v2
import ksqlite.sqlite3_txn_state as jni_sqlite3_txn_state
import ksqlite.sqlite3_update_hook as jni_sqlite3_update_hook
import ksqlite.sqlite3_uri_boolean as jni_sqlite3_uri_boolean
import ksqlite.sqlite3_uri_int64 as jni_sqlite3_uri_int64
import ksqlite.sqlite3_uri_key as jni_sqlite3_uri_key
import ksqlite.sqlite3_uri_parameter as jni_sqlite3_uri_parameter
import ksqlite.sqlite3_value_blob as jni_sqlite3_value_blob
import ksqlite.sqlite3_value_bytes as jni_sqlite3_value_bytes
import ksqlite.sqlite3_value_double as jni_sqlite3_value_double
import ksqlite.sqlite3_value_dup as jni_sqlite3_value_dup
import ksqlite.sqlite3_value_encoding as jni_sqlite3_value_encoding
import ksqlite.sqlite3_value_free as jni_sqlite3_value_free
import ksqlite.sqlite3_value_frombind as jni_sqlite3_value_frombind
import ksqlite.sqlite3_value_int as jni_sqlite3_value_int
import ksqlite.sqlite3_value_int64 as jni_sqlite3_value_int64
import ksqlite.sqlite3_value_nochange as jni_sqlite3_value_nochange
import ksqlite.sqlite3_value_numeric_type as jni_sqlite3_value_numeric_type
import ksqlite.sqlite3_value_subtype as jni_sqlite3_value_subtype
import ksqlite.sqlite3_value_text as jni_sqlite3_value_text
import ksqlite.sqlite3_value_type as jni_sqlite3_value_type
import ksqlite.sqlite3_vfs_find as jni_sqlite3_vfs_find
import ksqlite.sqlite3_vfs_register as jni_sqlite3_vfs_register
import ksqlite.sqlite3_vfs_unregister as jni_sqlite3_vfs_unregister
import ksqlite.sqlite3_vtab_collation as jni_sqlite3_vtab_collation
import ksqlite.sqlite3_vtab_config as jni_sqlite3_vtab_config
import ksqlite.sqlite3_vtab_distinct as jni_sqlite3_vtab_distinct
import ksqlite.sqlite3_vtab_in as jni_sqlite3_vtab_in
import ksqlite.sqlite3_vtab_in_first as jni_sqlite3_vtab_in_first
import ksqlite.sqlite3_vtab_in_next as jni_sqlite3_vtab_in_next
import ksqlite.sqlite3_vtab_nochange as jni_sqlite3_vtab_nochange
import ksqlite.sqlite3_vtab_on_conflict as jni_sqlite3_vtab_on_conflict
import ksqlite.sqlite3_vtab_rhs_value as jni_sqlite3_vtab_rhs_value
import ksqlite.sqlite3_wal_autocheckpoint as jni_sqlite3_wal_autocheckpoint
import ksqlite.sqlite3_wal_checkpoint as jni_sqlite3_wal_checkpoint
import ksqlite.sqlite3_wal_checkpoint_v2 as jni_sqlite3_wal_checkpoint_v2
import ksqlite.sqlite3_wal_hook as jni_sqlite3_wal_hook

///////////////////////////////////////////////////////////////////////////
// Library
///////////////////////////////////////////////////////////////////////////

/**
 * Workaround to load the native library at file level.
 */
@Suppress("unused")
private val nativeInit = run {
    ksqliteLoadLibrary()
}

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the raw values of `this` [VariadicValue] array.
 */
private fun Array<out VariadicValue<Any>?>.toJniJavaObjectArray(): Array<Any?> {
    return map { it?.value }.toTypedArray()
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

public actual fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result =
    autoExtensionRegister(callback) { jni_ksqlite_auto_extension(SharedAutoExtensionHandler) }

public actual fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?,
    callback: Sqlite3AutoVacuumPagesCallback<AppData>?
): Sqlite3Result = convertResult(
    jni_sqlite3_autovacuum_pages(
        db.pointer,
        callbackHandler(callback, appData, ::AutoVacuumPagesHandler),
        destructorHandler(appData, destroy)
    )
)

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result =
    convertResult(jni_sqlite3_backup_finish(backup.pointer))

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = jni_sqlite3_backup_init(
    destDb.pointer,
    destDbName,
    srcDb.pointer,
    srcDbName
).wrapOrNull(::sqlite3_backup)

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    jni_sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    jni_sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result = convertResult(jni_sqlite3_backup_step(backup.pointer, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
): Sqlite3Result = convertResult(
    jni_sqlite3_bind_blob(stmt.pointer, index, bytes, size, destructorHandler(bytes, destroy))
)

public actual fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result = convertResult(
    jni_sqlite3_bind_blob64(
        stmt.pointer,
        index,
        buffer.pointer,
        size,
        destructorHandler(buffer, destroy)
    )
)

public actual fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Sqlite3Result = convertResult(jni_sqlite3_bind_double(stmt.pointer, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result = convertResult(jni_sqlite3_bind_int(stmt.pointer, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result = convertResult(jni_sqlite3_bind_int64(stmt.pointer, index, value))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result = convertResult(jni_sqlite3_bind_null(stmt.pointer, index))

public actual fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int =
    jni_sqlite3_bind_parameter_count(stmt.pointer)

public actual fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int = jni_sqlite3_bind_parameter_index(stmt.pointer, name)

public actual fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = jni_sqlite3_bind_parameter_name(stmt.pointer, index)

public actual fun <Data> sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Data,
    type: String?,
    destroy: Sqlite3DestroyCallback<Data>?
): Sqlite3Result = convertResult(
    jni_sqlite3_bind_pointer(stmt.pointer, index, data, type, destructorHandler(data, destroy))
)

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    value: String
): Sqlite3Result = convertResult(jni_sqlite3_bind_text(stmt.pointer, index, value))

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result = convertResult(
    jni_sqlite3_bind_text64(
        stmt.pointer,
        index,
        buffer.pointer,
        size,
        destructorHandler(buffer, destroy),
        encoding.utf8OrThrow().value
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(jni_sqlite3_bind_value(stmt.pointer, index, value.pointer))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result = convertResult(jni_sqlite3_bind_zeroblob(stmt.pointer, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result = convertResult(jni_sqlite3_bind_zeroblob64(stmt.pointer, index, size.toLong()))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    jni_sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result =
    convertResult(jni_sqlite3_blob_close(blob.pointer))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Sqlite3BlobOpenFlag,
    outBlob: Sqlite3BlobOutputParam
): Sqlite3Result = convertResult(
    useParam(outBlob) { blobPtr ->
        jni_sqlite3_blob_open(
            db.pointer,
            databaseName,
            tableName,
            columnName,
            rowIndex,
            flags.value,
            blobPtr
        )
    }
)

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    bytes: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result =
    convertResult(jni_sqlite3_blob_read(blob.pointer, bytes, size, offset))

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result = convertResult(jni_sqlite3_blob_reopen(blob.pointer, rowIndex))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    bytes: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result = convertResult(jni_sqlite3_blob_write(blob.pointer, bytes, size, offset))

public actual fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3BusyHandlerCallback<AppData>?
): Sqlite3Result = convertResult(
    jni_sqlite3_busy_handler(
        db.pointer,
        callbackHandler(callback, appData, ::BusyHandlerHandler)
    )
)

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result = convertResult(jni_sqlite3_busy_timeout(db.pointer, millis))

public actual fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int =
    autoExtensionUnregister(callback) { ksqlite_cancel_auto_extension(SharedAutoExtensionHandler) }

public actual fun sqlite3_changes(db: sqlite3): Int =
    jni_sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    jni_sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result =
    commonClearBindings(stmt, jni_sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { jni_sqlite3_close(it?.pointer ?: 0) }

public actual fun sqlite3_close_v2(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { jni_sqlite3_close_v2(it?.pointer ?: 0) }

public actual fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CollationNeededCallback<AppData>?,
): Sqlite3Result = convertResult(
    jni_sqlite3_collation_needed(
        db.pointer,
        callbackHandler(callback, appData, ::CollationNeededHandler)
    )
)

public actual fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): ByteArray? = jni_sqlite3_column_blob(stmt.pointer, index)

public actual fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int = jni_sqlite3_column_bytes(stmt.pointer, index)

public actual fun sqlite3_column_count(stmt: sqlite3_stmt): Int =
    jni_sqlite3_column_count(stmt.pointer)

public actual fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = jni_sqlite3_column_database_name(stmt.pointer, index)

public actual fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String? = jni_sqlite3_column_decltype(stmt.pointer, index)

public actual fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double = jni_sqlite3_column_double(stmt.pointer, index)

public actual fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int = jni_sqlite3_column_int(stmt.pointer, index)

public actual fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long = jni_sqlite3_column_int64(stmt.pointer, index)

public actual fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = jni_sqlite3_column_name(stmt.pointer, index)

public actual fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = jni_sqlite3_column_origin_name(stmt.pointer, index)

public actual fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = jni_sqlite3_column_table_name(stmt.pointer, index)

public actual fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String? = jni_sqlite3_column_text(stmt.pointer, index)

public actual fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3DataType = convertDataType(jni_sqlite3_column_type(stmt.pointer, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = jni_sqlite3_column_value(stmt.pointer, index)
    .wrapOrNull(::sqlite3_value)

public actual fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CommitHookCallback<AppData>?
) {
    val _ = jni_sqlite3_commit_hook(
        db.pointer,
        callbackHandler(callback, appData, ::CommitHookHandler)
    )
}

public actual fun sqlite3_compileoption_get(index: Int): String? =
    jni_sqlite3_compileoption_get(index)

public actual fun sqlite3_compileoption_used(optName: String): Int =
    jni_sqlite3_compileoption_used(optName)

public actual fun sqlite3_complete(sql: String): Sqlite3CompleteResult =
    convertCompleteResult(jni_sqlite3_complete(sql))

public actual fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result = commonConfig(
    option = option,
    logFunctionPointer = { cb, appData -> callbackHandler(cb, appData, ::ConfigLogHandler) },
    sqllogFunctionPointer = { cb, appData -> callbackHandler(cb, appData, ::ConfigSqlLogHandler) },
    bufferPointer = Buffer::pointer,
    keyedStableRefPointer = null,
    rowidInView = {
        useParam(param) { paramPtr ->
            jni_sqlite3_config(id, arrayOf(paramPtr))
        }
    },
    nativeConfig = { id, values ->
        jni_sqlite3_config(id, values.toJniJavaObjectArray())
    }
)

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3? =
    jni_sqlite3_context_db_handle(context.pointer)
        .wrapOrNull(::sqlite3)

public actual fun <AppData> sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?,
    callback: Sqlite3CollationCompareCallback<AppData>?
): Sqlite3Result = convertResult(
    jni_sqlite3_create_collation_v2(
        db.pointer,
        name,
        encoding.utf8OrThrow().value,
        destructorHandler(appData, destroy),
        callbackHandler(callback, appData, ::CollationCompareHandler)
    )
)

public actual fun <AppData> sqlite3_create_function_v2(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    appData: AppData,
    func: Sqlite3FunctionFuncCallback<AppData>?,
    step: Sqlite3FunctionStepCallback<AppData>?,
    final: Sqlite3FunctionFinalCallback<AppData>?,
    destroy: Sqlite3DestroyCallback<AppData>?
): Sqlite3Result = convertResult(
    appFunction(appData, func, step, final, destroy) { fn, fnDestroy ->
        jni_sqlite3_create_function_v2(
            db.pointer,
            name,
            nArg,
            encoding.utf8OrThrow().value,
            fn,
            callbackHandler(fn, null, ::FunctionFuncHandler),
            callbackHandler(fn, null, ::FunctionStepHandler),
            callbackHandler(fn, null, ::FunctionFinalHandler),
            destructorHandler(fn, fnDestroy)
        )
    }
)

/*
public actual fun <AppData> sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module<AppData>?,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?
): Sqlite3Result = convertResult(
    jni_sqlite3_create_module_v2(
        db.pointer,
        name,
        module?.pointer.notNull,
        appData,
        destructorHandler(appData, destroy)
    )
)
*/
public actual fun <AppData> sqlite3_create_window_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    appData: AppData,
    step: Sqlite3FunctionStepCallback<AppData>?,
    final: Sqlite3FunctionFinalCallback<AppData>?,
    value: Sqlite3FunctionValueCallback<AppData>?,
    inverse: Sqlite3FunctionInverseCallback<AppData>?,
    destroy: Sqlite3DestroyCallback<AppData>?
): Sqlite3Result = convertResult(
    appWindowFunction(appData, step, final, value, inverse, destroy) { fn, fnDestroy ->
        jni_sqlite3_create_window_function(
            db.pointer,
            name,
            nArg,
            encoding.utf8OrThrow().value,
            fn,
            callbackHandler(fn, null, ::FunctionStepHandler),
            callbackHandler(fn, null, ::FunctionFinalHandler),
            callbackHandler(fn, null, ::FunctionValueHandler),
            callbackHandler(fn, null, ::FunctionInverseHandler),
            destructorHandler(fn, fnDestroy)
        )
    }
)

public actual fun sqlite3_data_count(stmt: sqlite3_stmt): Int =
    jni_sqlite3_data_count(stmt.pointer)

public actual fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result =
    convertResult(jni_sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: Sqlite3DbConfigOption,
): Sqlite3Result = commonDbConfig(
    option = option,
    bufferPointer = Buffer::pointer,
    outParamConfig = {
        useParam(state) { statePtr ->
            jni_sqlite3_db_config(db.pointer, id, arrayOf(value, statePtr))
        }
    },
    nativeConfig = { id, values ->
        jni_sqlite3_db_config(db.pointer, id, values.toJniJavaObjectArray())
    }
)

public actual fun sqlite3_db_filename(
    db: sqlite3,
    name: String
): sqlite3_filename? = jni_sqlite3_db_filename(db.pointer, name)

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    jni_sqlite3_db_handle(stmt.pointer)
        .wrapOrNull(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = jni_sqlite3_db_name(db.pointer, index)

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    name: String
): Int = jni_sqlite3_db_readonly(db.pointer, name)

public actual fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result =
    convertResult(jni_sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    jni_sqlite3_db_status(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    jni_sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result = convertResult(jni_sqlite3_declare_vtab(db.pointer, sql))

public actual fun sqlite3_deserialize(
    db: sqlite3,
    schema: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: Sqlite3DeserializeFlag?
): Sqlite3Result = convertResult(
    jni_sqlite3_deserialize(
        db.pointer,
        schema,
        buffer.pointer,
        dbSize,
        bufferSize,
        flags?.value ?: 0
    )
)

/*public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result = convertResult(memScoped {
    jni_sqlite3_drop_modules(db.pointer, keep.allocateUtf8Array())
})*/

public actual fun sqlite3_errcode(db: sqlite3): Int = jni_sqlite3_errcode(db.pointer)

public actual fun sqlite3_errmsg(db: sqlite3): String? = jni_sqlite3_errmsg(db.pointer)

public actual fun sqlite3_error_offset(db: sqlite3): Int = jni_sqlite3_error_offset(db.pointer)

public actual fun sqlite3_errstr(resultCode: Int): String? = jni_sqlite3_errstr(resultCode)

public actual fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: Sqlite3ExecCallback<AppData>?
): Sqlite3Result = convertResult(
    useParam(outErrorMessage) { errorMessagePtr ->
        jni_sqlite3_exec(
            db.pointer,
            sql,
            callbackHandler(callback, appData, ::ExecHandler),
            errorMessagePtr
        )
    }
)

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? =
    jni_sqlite3_expanded_sql(stmt.pointer)

public actual fun sqlite3_extended_errcode(db: sqlite3): Int =
    jni_sqlite3_extended_errcode(db.pointer)

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result = convertResult(jni_sqlite3_extended_result_codes(db.pointer, enabled))

public actual fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode
): Sqlite3Result = convertResult(jni_sqlite3_file_control(db.pointer, name, opcode.code))

public actual fun sqlite3_finalize(stmt: sqlite3_stmt?): Sqlite3Result =
    stmt.deallocateNullable { jni_sqlite3_finalize(stmt?.pointer.notNull) }

public actual fun sqlite3_free(buffer: Buffer?): Unit =
    jni_sqlite3_free(buffer?.pointer.notNull)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    jni_sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    jni_sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): Sqlite3Result =
    convertResult(jni_sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    jni_sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    jni_sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(jni_sqlite3_key(db.pointer, key, nKey))

public actual fun sqlite3_key_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(jni_sqlite3_key_v2(db.pointer, dbName, key, nKey))

public actual fun sqlite3_keyword_check(word: String): Int = jni_sqlite3_keyword_check(word)

public actual fun sqlite3_keyword_count(): Int =
    jni_sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): Sqlite3Result = convertResult(useParam(outName) { namePtr ->
    jni_sqlite3_keyword_name(index, namePtr)
})

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    jni_sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    jni_sqlite3_libversion()

public actual fun sqlite3_libversion_number(db: sqlite3): Int =
    jni_sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: Sqlite3Limit,
    newVal: Int
): Int = jni_sqlite3_limit(db.pointer, id.id, newVal)

public actual fun sqlite3_log(
    errorCode: Int,
    message: String
): Unit = jni_sqlite3_log(errorCode, message)

public actual fun sqlite3_malloc(size: Int): Buffer? =
    jni_sqlite3_malloc(size).orNull?.let { Buffer.from(it, size.toLong()) }

public actual fun sqlite3_malloc64(size: Long): Buffer? =
    jni_sqlite3_malloc64(size).orNull?.let { Buffer.from(it, size) }

public actual fun sqlite3_memory_used(): Long =
    jni_sqlite3_memory_used()

public actual fun sqlite3_memory_highwater(resetFlag: Int): Long =
    jni_sqlite3_memory_highwater(resetFlag)

public actual fun sqlite3_msize(buffer: Buffer?): ULong =
    jni_sqlite3_msize(buffer?.pointer.notNull).toULong()

public actual fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt?
): sqlite3_stmt? = jni_sqlite3_next_stmt(db.pointer, stmt?.pointer.notNull)
    .wrapOrNull(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: Sqlite3OutputParam
): Sqlite3Result = convertResult(useParam(outDb) { dbPtr ->
    jni_sqlite3_open(fileName, dbPtr!!)
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: Sqlite3OutputParam,
    flags: Sqlite3OpenFlag.Db,
    vfs: String?
): Sqlite3Result = convertResult(useParam(outDb) { dbPtr ->
    jni_sqlite3_open_v2(fileName, dbPtr!!, flags.value, vfs)
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): Sqlite3Result = convertResult(jni_sqlite3_overload_function(db.pointer, name, nArg))

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result = convertResult(useParams(outStmt, outOffset) { stmtPtr, offsetPtr ->
    ksqlite_prepare_v2(db.pointer, sql, maxBytes, stmtPtr!!, offsetPtr)
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result = convertResult(useParam(outStmt) { stmtPtr ->
    jni_sqlite3_prepare_v2(db.pointer, sql, stmtPtr!!)
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result = convertResult(useParams(outStmt, outOffset) { stmtPtr, offsetPtr ->
    val prepFlags = flags?.value ?: 0
    ksqlite_prepare_v3(db.pointer, sql, maxBytes, prepFlags, stmtPtr!!, offsetPtr)
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result = convertResult(useParam(outStmt) { stmtPtr ->
    val prepFlags = flags?.value ?: 0
    jni_sqlite3_prepare_v3(db.pointer, sql, prepFlags, stmtPtr!!)
})

public actual fun sqlite3_preupdate_blobwrite(db: sqlite3): Int =
    jni_sqlite3_preupdate_blobwrite(db.pointer)

public actual fun sqlite3_preupdate_count(db: sqlite3): Int =
    jni_sqlite3_preupdate_count(db.pointer)

public actual fun sqlite3_preupdate_depth(db: sqlite3): Int =
    jni_sqlite3_preupdate_depth(db.pointer)

public actual fun <AppData> sqlite3_preupdate_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3PreupdateHookCallback<AppData>?
) {
    val _ = jni_sqlite3_preupdate_hook(
        db.pointer,
        callbackHandler(callback, appData, ::PreupdateHookHandler),
    )
}

public actual fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result = convertResult(useParam(outValue) { valuePtr ->
    jni_sqlite3_preupdate_new(db.pointer, index, valuePtr!!)
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result = convertResult(useParam(outValue) { valuePtr ->
    jni_sqlite3_preupdate_old(db.pointer, index, valuePtr!!)
})

public actual fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: Sqlite3ProgressHandlerCallback<AppData>?
) {
    val _ = jni_sqlite3_progress_handler(
        db.pointer,
        nOps,
        callbackHandler(callback, appData, ::ProgressHandlerHandler),
    )
}

public actual fun sqlite3_randomness(
    size: Int,
    buffer: Buffer
): Unit = jni_sqlite3_randomness(size, buffer.pointer)

public actual fun sqlite3_realloc(
    buffer: Buffer,
    size: Int
): Buffer? = Buffer.from(
    pointer = jni_sqlite3_realloc(buffer.pointer, size),
    size = size.toLong()
)

public actual fun sqlite3_realloc64(
    buffer: Buffer,
    size: Long
): Buffer? = Buffer.from(
    pointer = jni_sqlite3_realloc64(buffer.pointer, size),
    size = size
)

public actual fun sqlite3_rekey(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(jni_sqlite3_rekey(db.pointer, key, nKey))

public actual fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result = convertResult(jni_sqlite3_rekey_v2(db.pointer, dbName, key, nKey))

public actual fun sqlite3_release_memory(size: Int): Int =
    jni_sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(jni_sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { jni_sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
): Unit = jni_sqlite3_result_blob(
    context.pointer,
    bytes,
    size,
    destructorHandler(bytes, destroy)
)

public actual fun sqlite3_result_blob64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Unit = jni_sqlite3_result_blob64(
    context.pointer,
    buffer.pointer,
    size,
    destructorHandler(buffer, destroy)
)

public actual fun sqlite3_result_double(
    context: sqlite3_context,
    value: Double
): Unit = jni_sqlite3_result_double(context.pointer, value)

public actual fun sqlite3_result_error(
    context: sqlite3_context,
    message: String
): Unit = jni_sqlite3_result_error(context.pointer, message)

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    errorCode: Int
): Unit = jni_sqlite3_result_error_code(context.pointer, errorCode)

public actual fun sqlite3_result_error_nomem(context: sqlite3_context): Unit =
    jni_sqlite3_result_error_nomem(context.pointer)

public actual fun sqlite3_result_error_toobig(context: sqlite3_context): Unit =
    jni_sqlite3_result_error_toobig(context.pointer)

public actual fun sqlite3_result_int(
    context: sqlite3_context,
    value: Int
): Unit = jni_sqlite3_result_int(context.pointer, value)

public actual fun sqlite3_result_int64(
    context: sqlite3_context,
    value: Long
): Unit = jni_sqlite3_result_int64(context.pointer, value)

public actual fun sqlite3_result_null(context: sqlite3_context): Unit =
    jni_sqlite3_result_null(context.pointer)

public actual fun <Data> sqlite3_result_pointer(
    context: sqlite3_context,
    data: Data,
    type: String?,
    destroy: Sqlite3DestroyCallback<Data>?
): Unit = jni_sqlite3_result_pointer(context.pointer, data, type, destructorHandler(data, destroy))

public actual fun sqlite3_result_subtype(
    context: sqlite3_context,
    subtype: UInt
): Unit = jni_sqlite3_result_subtype(context.pointer, subtype.toInt())

public actual fun sqlite3_result_text(
    context: sqlite3_context,
    value: String
): Unit = jni_sqlite3_result_text(context.pointer, value)

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Unit = jni_sqlite3_result_text64(
    context.pointer,
    buffer.pointer,
    size,
    destructorHandler(buffer, destroy),
    encoding.utf8OrThrow().value
)

public actual fun sqlite3_result_value(
    context: sqlite3_context,
    value: sqlite3_value,
): Unit = jni_sqlite3_result_value(context.pointer, value.pointer)

public actual fun sqlite3_result_zeroblob(
    context: sqlite3_context,
    size: Int
): Unit = jni_sqlite3_result_zeroblob(context.pointer, size)

public actual fun sqlite3_result_zeroblob64(
    context: sqlite3_context,
    size: ULong
): Sqlite3Result = convertResult(jni_sqlite3_result_zeroblob64(context.pointer, size.toLong()))

public actual fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3RollbackHookCallback<AppData>?
) {
    val _ = jni_sqlite3_rollback_hook(
        db.pointer,
        callbackHandler(callback, appData, ::RollbackHookHandler)
    )
}

public actual fun sqlite3_serialize(
    db: sqlite3,
    schema: String?,
    flags: Sqlite3SerializeFlag?
): Buffer? {
    val size = Int64OutputParam(0)

    val pointer = useParam(size) { sizePtr ->
        jni_sqlite3_serialize(db.pointer, schema, sizePtr!!, flags?.value ?: 0)
    }

    return Buffer.from(pointer, size.value)
}

public actual fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3AuthorizerCallback<AppData>?
): Sqlite3Result = convertResult(
    jni_sqlite3_set_authorizer(
        db.pointer,
        callbackHandler(callback, appData, ::AuthorizerHandler)
    )
)

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: Sqlite3Result.Failure,
    message: String?
): Sqlite3Result = convertResult(jni_sqlite3_set_errmsg(db.pointer, errorCode.code, message))

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = jni_sqlite3_set_last_insert_rowid(db.pointer, rowId)

public actual fun sqlite3_shutdown(): Sqlite3Result =
    convertResult(jni_sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = jni_sqlite3_snapshot_cmp(snapshot1.pointer, snapshot2.pointer)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    jni_sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: Sqlite3SnapshotOutputParam
): Sqlite3Result = convertResult(useParam(outSnapshot) { snapshotPtr ->
    jni_sqlite3_snapshot_get(db.pointer, name, snapshotPtr!!)
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): Sqlite3Result = convertResult(jni_sqlite3_snapshot_open(db.pointer, name, snapshot.pointer))

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(jni_sqlite3_snapshot_recover(db.pointer, name))

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    jni_sqlite3_soft_heap_limit64(limit)

public actual fun sqlite3_sourceid(): String =
    jni_sqlite3_sourceid()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    jni_sqlite3_sql(stmt.pointer)

public actual fun sqlite3_status(
    option: Sqlite3StatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    jni_sqlite3_status(option.id, curPtr!!, highPtr!!, resetFlag)
})

public actual fun sqlite3_status64(
    option: Sqlite3StatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    jni_sqlite3_status64(option.id, curPtr!!, highPtr!!, resetFlag)
})

public actual fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(jni_sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    jni_sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: Sqlite3ExplainMode
): Sqlite3Result = convertResult(jni_sqlite3_stmt_explain(stmt.pointer, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode =
    convertExplainMode(jni_sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    jni_sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: Sqlite3StatementStatusCounter,
    resetFlag: Int
): Int = jni_sqlite3_stmt_status(stmt.pointer, counter.id, resetFlag)

public actual fun sqlite3_strglob(
    globPattern: String,
    input: String
): Int = jni_sqlite3_strglob(globPattern, input)

public actual fun sqlite3_stricmp(
    first: String,
    second: String
): Int = jni_sqlite3_stricmp(first, second)

public actual fun sqlite3_strlike(
    likePattern: String,
    input: String,
    escapeCharacter: Char
): Int = jni_sqlite3_strlike(likePattern, input, escapeCharacter.code)

public actual fun sqlite3_strnicmp(
    first: String,
    second: String,
    maxCharacters: Int
): Int = jni_sqlite3_strnicmp(first, second, maxCharacters)

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    jni_sqlite3_system_errno(db.pointer)

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
): Sqlite3Result {
    val dataTypePtr = outDataType?.attach()
    val collationNamePtr = outCollationName?.attach()
    val notNullPtr = outNotNull?.attach()
    val primaryKeyPtr = outPrimaryKey?.attach()
    val autoIncrementPtr = outAutoIncrement?.attach()

    val resultCode = try {
        jni_sqlite3_table_column_metadata(
            db.pointer,
            dbName,
            tableName,
            columnName,
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

    return convertResult(resultCode)
}

public actual fun sqlite3_total_changes(db: sqlite3): Int =
    jni_sqlite3_total_changes(db.pointer)

public actual fun sqlite3_total_changes64(db: sqlite3): Long =
    jni_sqlite3_total_changes64(db.pointer)

public actual fun <AppData> sqlite3_trace_v2(
    db: sqlite3,
    mask: Sqlite3TraceCode?,
    appData: AppData,
    callback: Sqlite3TraceCallback<AppData>?
): Sqlite3Result = convertResult(
    jni_sqlite3_trace_v2(
        db.pointer,
        mask?.value ?: 0,
        callbackHandler(callback, appData, ::TraceHandler)
    )
)

public actual fun sqlite3_txn_state(
    db: sqlite3,
    schema: String?
): Sqlite3TransactionState? = convertTransactionState(jni_sqlite3_txn_state(db.pointer, schema))

public actual fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3UpdateHookCallback<AppData>?
) {
    val _ = jni_sqlite3_update_hook(
        db.pointer,
        callbackHandler(callback, appData, ::UpdateHookHandler)
    )
}

public actual fun sqlite3_uri_boolean(
    fileName: sqlite3_filename,
    parameter: String,
    default: Int
): Int = jni_sqlite3_uri_boolean(fileName, parameter, default)

public actual fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long = jni_sqlite3_uri_int64(fileName, parameter, default)

public actual fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String? = jni_sqlite3_uri_key(fileName, index)

public actual fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String? = jni_sqlite3_uri_parameter(fileName, parameter)

public actual fun sqlite3_value_blob(value: sqlite3_value): ByteArray? =
    jni_sqlite3_value_blob(value.pointer)

public actual fun sqlite3_value_bytes(value: sqlite3_value): Int =
    jni_sqlite3_value_bytes(value.pointer)

public actual fun sqlite3_value_double(value: sqlite3_value): Double =
    jni_sqlite3_value_double(value.pointer)

public actual fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value? =
    jni_sqlite3_value_dup(value.pointer).wrapOrNull(::sqlite3_value)

public actual fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2? =
    convertTextEncoding(jni_sqlite3_value_encoding(value.pointer))

public actual fun sqlite3_value_free(value: sqlite3_value): Unit =
    jni_sqlite3_value_free(value.pointer)

public actual fun sqlite3_value_frombind(value: sqlite3_value): Int =
    jni_sqlite3_value_frombind(value.pointer)

public actual fun sqlite3_value_int(value: sqlite3_value): Int =
    jni_sqlite3_value_int(value.pointer)

public actual fun sqlite3_value_int64(value: sqlite3_value): Long =
    jni_sqlite3_value_int64(value.pointer)

public actual fun sqlite3_value_nochange(value: sqlite3_value): Int =
    jni_sqlite3_value_nochange(value.pointer)

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(jni_sqlite3_value_numeric_type(value.pointer))

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    jni_sqlite3_value_subtype(value.pointer).toUInt()

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    jni_sqlite3_value_text(value.pointer)

public actual fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(jni_sqlite3_value_type(value.pointer))

/*public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? = memScoped {
    jni_sqlite3_vfs_find(name.allocateUtf8()).orNull?.let(::sqlite3_vfs)
}

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): Sqlite3Result = convertResult(jni_sqlite3_vfs_register(vfs.pointer, makeDefault))

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): Sqlite3Result =
    convertResult(jni_sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String? = jni_sqlite3_vtab_collation(info.pointer, index)
    .toKStringFromUtf8OrNull()

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: Sqlite3VirtualTableConfigOption
): Sqlite3Result = commonVtabConfig(option) { id, values ->
    invokeVariadic(values) { layouts, arguments ->
        jni_sqlite3_vtab_config
            .makeInvoker(*layouts)
            .apply(db.pointer, id, *arguments)
    }
}

public actual fun sqlite3_vtab_distinct(info: sqlite3_index_info): Int =
    jni_sqlite3_vtab_distinct(info.pointer)

public actual fun sqlite3_vtab_in(
    info: sqlite3_index_info,
    index: Int,
    handle: Int
): Int = jni_sqlite3_vtab_in(info.pointer, index, handle)

public actual fun sqlite3_vtab_in_first(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    jni_sqlite3_vtab_in_first(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    jni_sqlite3_vtab_in_next(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    jni_sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): Sqlite3Result =
    convertResult(jni_sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    jni_sqlite3_vtab_rhs_value(info.pointer, index, valuePtr)
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): Sqlite3Result = convertResult(jni_sqlite3_wal_autocheckpoint(db.pointer, nFrame))

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(memScoped {
    jni_sqlite3_wal_checkpoint(db.pointer, name.allocateUtf8())
})

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    name: String?,
    mode: Sqlite3CheckpointMode,
    outNLog: Int32OutputParam?,
    outNCkpt: Int32OutputParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
        jni_sqlite3_wal_checkpoint_v2(
            db.pointer,
            name.allocateUtf8(),
            mode.id,
            nLogPtr,
            nCkptPtr
        )
    }
})

public actual fun <AppData> sqlite3_wal_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3WalHookCallback<AppData>?
): Unit = db.withMemoryManager {
    jni_sqlite3_wal_hook(
        db.pointer,
        callbackHandler(callback, appData, ::WalHookHandler),
        keyedStableRefPointer(KEY_WAL_HOOK, callback, appData)
    )
}*/