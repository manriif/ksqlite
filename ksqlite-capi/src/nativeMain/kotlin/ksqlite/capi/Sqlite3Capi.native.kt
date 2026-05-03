@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCStringArray
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.SQLITE_TRANSIENT
import ksqlite.capi.handlers.AutoVacuumPagesHandler
import ksqlite.capi.handlers.BusyHandlerHandler
import ksqlite.capi.handlers.CollationNeededHandler
import ksqlite.capi.handlers.CommitHookHandler
import ksqlite.capi.handlers.ConfigLogHandler
import ksqlite.capi.handlers.ConfigSqlLogHandler
import ksqlite.capi.handlers.CreateCollationHandler
import ksqlite.capi.handlers.CreateFunctionFinalHandler
import ksqlite.capi.handlers.CreateFunctionFuncHandler
import ksqlite.capi.handlers.CreateFunctionInverseHandler
import ksqlite.capi.handlers.CreateFunctionStepHandler
import ksqlite.capi.handlers.CreateFunctionValueHandler
import ksqlite.capi.handlers.ExecHandler
import ksqlite.capi.handlers.PreupdateHookHandler
import ksqlite.capi.handlers.ProgressHandlerHandler
import ksqlite.capi.handlers.RollbackHookHandler
import ksqlite.capi.handlers.SetAuthorizerHandler
import ksqlite.capi.handlers.SharedExtensionHandler
import ksqlite.capi.handlers.TraceHandler
import ksqlite.capi.handlers.UpdateHookHandler
import ksqlite.capi.handlers.WalHookHandler
import ksqlite.capi.handlers.handle
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.deallocateNullable
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.keyedStableRefPointer
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.memory.userDataDisposer
import ksqlite.capi.types.Sqlite3AutoExtensionCallback
import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobOutParam
import ksqlite.capi.types.Sqlite3BusyHandlerCallback
import ksqlite.capi.types.Sqlite3CheckpointMode
import ksqlite.capi.types.Sqlite3CollationNeededCallback
import ksqlite.capi.types.Sqlite3CommitHookCallback
import ksqlite.capi.types.Sqlite3CompleteResult
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3CreateCollationCallback
import ksqlite.capi.types.Sqlite3CreateFunctionFinalCallback
import ksqlite.capi.types.Sqlite3CreateFunctionFuncCallback
import ksqlite.capi.types.Sqlite3CreateFunctionInverseCallback
import ksqlite.capi.types.Sqlite3CreateFunctionStepCallback
import ksqlite.capi.types.Sqlite3CreateFunctionValueCallback
import ksqlite.capi.types.Sqlite3DataType
import ksqlite.capi.types.Sqlite3DatabaseConnectionOutParam
import ksqlite.capi.types.Sqlite3DbConfigOption
import ksqlite.capi.types.Sqlite3DbStatusOption
import ksqlite.capi.types.Sqlite3DeserializeFlag
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3ExecCallback
import ksqlite.capi.types.Sqlite3ExplainMode
import ksqlite.capi.types.Sqlite3FileControlOpcode
import ksqlite.capi.types.Sqlite3FileOpenFlag
import ksqlite.capi.types.Sqlite3IntOutParam
import ksqlite.capi.types.Sqlite3Limit
import ksqlite.capi.types.Sqlite3LongOutParam
import ksqlite.capi.types.Sqlite3PrepareFlag
import ksqlite.capi.types.Sqlite3PreupdateHookCallback
import ksqlite.capi.types.Sqlite3ProgressHandlerCallback
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3RollbackHookCallback
import ksqlite.capi.types.Sqlite3SerializeFlag
import ksqlite.capi.types.Sqlite3SetAuthorizerCallback
import ksqlite.capi.types.Sqlite3SnapshotOutParam
import ksqlite.capi.types.Sqlite3StatementOutParam
import ksqlite.capi.types.Sqlite3StatementStatusCounter
import ksqlite.capi.types.Sqlite3StatusOption
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3TraceCallback
import ksqlite.capi.types.Sqlite3TraceCode
import ksqlite.capi.types.Sqlite3TransactionState
import ksqlite.capi.types.Sqlite3UpdateHookCallback
import ksqlite.capi.types.Sqlite3Utf8OutParam
import ksqlite.capi.types.Sqlite3ValueOutParam
import ksqlite.capi.types.Sqlite3VirtualTableConfigOption
import ksqlite.capi.types.Sqlite3WalHookCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_filename
import ksqlite.capi.types.sqlite3_index_info
import ksqlite.capi.types.sqlite3_module
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_pointer
import ksqlite.capi.types.sqlite3_snapshot
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.sqlite3_vfs
import ksqlite.capi.types.useParam
import ksqlite.capi.types.useParamMemScoped
import ksqlite.capi.types.useParams
import ksqlite.capi.types.useParamsMemScoped
import ksqlite.ksqlite_auto_extension
import ksqlite.ksqlite_cancel_auto_extension
import ksqlite.sqlite3_aggregate_context as native_sqlite3_aggregate_context
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
import ksqlite.sqlite3_create_collation as native_sqlite3_create_collation
import ksqlite.sqlite3_create_collation_v2 as native_sqlite3_create_collation_v2
import ksqlite.sqlite3_create_function as native_sqlite3_create_function
import ksqlite.sqlite3_create_function_v2 as native_sqlite3_create_function_v2
import ksqlite.sqlite3_create_module as native_sqlite3_create_module
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
import ksqlite.sqlite3_get_auxdata as native_sqlite3_get_auxdata
import ksqlite.sqlite3_hard_heap_limit64 as native_sqlite3_hard_heap_limit64
import ksqlite.sqlite3_initialize as native_sqlite3_initialize
import ksqlite.sqlite3_interrupt as native_sqlite3_interrupt
import ksqlite.sqlite3_is_interrupted as native_sqlite3_is_interrupted
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
import ksqlite.sqlite3_set_auxdata as native_sqlite3_set_auxdata
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
import ksqlite.sqlite3_user_data as native_sqlite3_user_data
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
import ksqlite.sqlite3_value_pointer as native_sqlite3_value_pointer
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
 */
private fun Array<out VariadicValue<COpaquePointer>?>.toVariadicArguments(): Array<out Any?> {
    return map { it?.value }.toTypedArray()
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

public actual fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native_sqlite3_aggregate_context(
        arg0 = context.pointer,
        nBytes = nBytes
    ),
    size = nBytes.toLong()
)

public actual fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result =
    autoExtensionRegister(callback) { ksqlite_auto_extension(SharedExtensionHandler) }

public actual fun sqlite3_autovacuum_pages(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3AutoVacuumPagesCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_autovacuum_pages(
        db = db.pointer,
        arg1 = AutoVacuumPagesHandler.handle(callback),
        arg2 = db.memory.keyedStableRefPointer(
            key = KEY_AUTOVACUUM_PAGES,
            data = callback,
            userData = userData,
            destructor = destructor
        ),
        arg3 = stableRefDisposer(callback, destructor)
    )
)

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result = convertResult(
    native_sqlite3_backup_finish(backup.pointer)
)

public actual fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup? = native_sqlite3_backup_init(
    pDest = destDb.pointer,
    zDestName = destDbName,
    pSource = srcDb.pointer,
    zSourceName = srcDbName
)?.let(::sqlite3_backup)

public actual fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int =
    native_sqlite3_backup_pagecount(backup.pointer)

public actual fun sqlite3_backup_remaining(backup: sqlite3_backup): Int =
    native_sqlite3_backup_remaining(backup.pointer)

public actual fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result = convertResult(
    native_sqlite3_backup_step(
        p = backup.pointer,
        nPage = nPage
    )
)

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    data: ByteArray?,
    size: Int,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_bind_blob(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = stmt.memory.byteArrayPointer(data, destructor),
        n = size,
        arg4 = globalDisposer(data)
    )
)

public actual fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    size: Long,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_bind_blob64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = data?.block?.pointer,
        arg3 = size.convert(),
        arg4 = userDataDisposer(data, destructor)
    )
)

public actual fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Sqlite3Result = convertResult(
    native_sqlite3_bind_double(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value
    )
)

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result = convertResult(
    native_sqlite3_bind_int(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value
    )
)

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result = convertResult(
    native_sqlite3_bind_int64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value
    )
)

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result = convertResult(
    native_sqlite3_bind_null(
        arg0 = stmt.pointer,
        arg1 = index,
    )
)

public actual fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int =
    native_sqlite3_bind_parameter_count(stmt.pointer)

public actual fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int = native_sqlite3_bind_parameter_index(
    arg0 = stmt.pointer,
    zName = name
)

public actual fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_bind_parameter_name(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    type: String?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(allocateNamedPointer(type, destructor) {
    native_sqlite3_bind_pointer(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = stmt.memory.stableRefPointer(this, data, disposer),
        arg3 = typePointer,
        arg4 = stableRefDisposer(this, disposer)
    )
})

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    text: String?,
    size: Int?
): Sqlite3Result = convertResult(memScoped {
    val cText = text?.cstr

    native_sqlite3_bind_text(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = cText?.ptr,
        arg3 = size ?: cText?.size ?: 0,
        arg4 = SQLITE_TRANSIENT
    )
})

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_mutable_pointer?,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_bind_text64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = data?.block?.pointer,
        arg3 = size.convert(),
        arg4 = userDataDisposer(data, destructor),
        encoding = encoding.utf8OrThrow().value.convert()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result = convertResult(
    native_sqlite3_bind_value(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = value.pointer
    )
)

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result = convertResult(
    native_sqlite3_bind_zeroblob(
        arg0 = stmt.pointer,
        arg1 = index,
        n = size
    )
)

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result = convertResult(
    native_sqlite3_bind_zeroblob64(
        arg0 = stmt.pointer,
        arg1 = index,
        arg2 = size
    )
)

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
    outBlob: Sqlite3BlobOutParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outBlob) { blobPtr ->
        native_sqlite3_blob_open(
            arg0 = db.pointer,
            zDb = databaseName.cstr.ptr,
            zTable = tableName.cstr.ptr,
            zColumn = columnName.cstr.ptr,
            iRow = rowIndex,
            flags = flags.value,
            ppBlob = blobPtr
        )
    }
})

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result = convertResult(
    native_sqlite3_blob_read(
        arg0 = blob.pointer,
        Z = buffer.refTo(0),
        N = size,
        iOffset = offset
    )
)

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result = convertResult(
    native_sqlite3_blob_reopen(
        arg0 = blob.pointer,
        arg1 = rowIndex
    )
)

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result = convertResult(
    native_sqlite3_blob_write(
        arg0 = blob.pointer,
        z = buffer.refTo(0),
        n = size,
        iOffset = offset
    )
)

public actual fun sqlite3_busy_handler(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3BusyHandlerCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_busy_handler(
        arg0 = db.pointer,
        arg1 = BusyHandlerHandler.handle(callback),
        arg2 = db.memory.keyedStableRefPointer(KEY_BUSY_HANDLER, callback, userData)
    )
)

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result = convertResult(
    native_sqlite3_busy_timeout(
        arg0 = db.pointer,
        ms = millis
    )
)

public actual fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int =
    autoExtensionUnregister(callback) { ksqlite_cancel_auto_extension(SharedExtensionHandler) }

public actual fun sqlite3_changes(db: sqlite3): Int =
    native_sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    native_sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result =
    commonSqlite3ClearBindings(stmt, native_sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { native_sqlite3_close(it?.pointer) }

public actual fun sqlite3_close_v2(db: sqlite3?): Sqlite3Result =
    db.deallocateNullable { native_sqlite3_close_v2(it?.pointer) }

public actual fun sqlite3_collation_needed(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CollationNeededCallback?,
): Sqlite3Result = convertResult(
    native_sqlite3_collation_needed(
        arg0 = db.pointer,
        arg1 = db.memory.keyedStableRefPointer(KEY_COLLATION_NEEDED, callback, userData),
        arg2 = CollationNeededHandler.handle(callback)
    )
)

public actual fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_pointer? = sqlite3_pointer.from(
    pointer = native_sqlite3_column_blob(
        arg0 = stmt.pointer,
        iCol = index
    ),
    size = native_sqlite3_column_bytes(
        arg0 = stmt.pointer,
        iCol = index
    ).toLong()
)

public actual fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int = native_sqlite3_column_bytes(
    arg0 = stmt.pointer,
    iCol = index
)

public actual fun sqlite3_column_count(stmt: sqlite3_stmt): Int =
    native_sqlite3_column_count(stmt.pointer)

public actual fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_database_name(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_decltype(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double = native_sqlite3_column_double(
    arg0 = stmt.pointer,
    iCol = index
)

public actual fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int = native_sqlite3_column_int(
    arg0 = stmt.pointer,
    iCol = index
)

public actual fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long = native_sqlite3_column_int64(
    arg0 = stmt.pointer,
    iCol = index
)

public actual fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_name(
    arg0 = stmt.pointer,
    N = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_origin_name(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_table_name(
    arg0 = stmt.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String? = native_sqlite3_column_text(
    arg0 = stmt.pointer,
    iCol = index
)?.reinterpret<ByteVar>()
    ?.toKStringFromUtf8()

public actual fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3DataType = convertDataType(
    native_sqlite3_column_type(
        arg0 = stmt.pointer,
        iCol = index
    )
)

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = native_sqlite3_column_value(
    arg0 = stmt.pointer,
    iCol = index
)?.let(::sqlite3_value)

public actual fun sqlite3_commit_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CommitHookCallback?
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.fromStableRef(
    native_sqlite3_commit_hook(
        arg0 = db.pointer,
        arg1 = CommitHookHandler.handle(callback),
        arg2 = db.memory.keyedStableRefPointer(KEY_COMMIT_HOOK, callback, userData)
    )
)

public actual fun sqlite3_compileoption_get(index: Int): String? =
    native_sqlite3_compileoption_get(index)?.toKStringFromUtf8()

public actual fun sqlite3_compileoption_used(optName: String): Int =
    native_sqlite3_compileoption_used(optName)

public actual fun sqlite3_complete(sql: String): Sqlite3CompleteResult =
    convertCompleteResult(native_sqlite3_complete(sql))

public actual fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result = commonSqlite3Config(
    option = option,
    logFunctionPointer = ConfigLogHandler::handle,
    sqllogFunctionPointer = ConfigSqlLogHandler::handle,
    memoryPointer = { it.block.pointer },
    keyedStableRefPointer = MemoryManager::keyedStableRefPointer,
    rowidInView = {
        useParamMemScoped(param) { paramPtr ->
            native_sqlite3_config(id, paramPtr)
        }
    },
    nativeConfig = { id, values ->
        native_sqlite3_config(id, *values.toVariadicArguments())
    }
)

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3? =
    native_sqlite3_context_db_handle(context.pointer)?.let(::sqlite3)

public actual fun sqlite3_create_collation(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3CreateCollationCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_create_collation(
        arg0 = db.pointer,
        zName = name,
        eTextRep = encoding.utf8OrThrow().value,
        pArg = db.memory.keyedStableRefPointer(KEY_CREATE_COLLATION, callback, userData),
        xCompare = CreateCollationHandler.handle(callback)
    )
)

public actual fun sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3CreateCollationCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_create_collation_v2(
        arg0 = db.pointer,
        zName = name,
        eTextRep = encoding.utf8OrThrow().value,
        pArg = db.memory.keyedStableRefPointer(
            key = KEY_CREATE_COLLATION,
            data = callback,
            userData = userData,
            destructor = destructor
        ),
        xCompare = CreateCollationHandler.handle(callback),
        xDestroy = stableRefDisposer(callback, destructor)
    )
)

public actual fun sqlite3_create_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    userData: sqlite3_mutable_pointer?,
    func: Sqlite3CreateFunctionFuncCallback?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_create_function(
        db = db.pointer,
        zFunctionName = name,
        nArg = nArg,
        eTextRep = encoding.utf8OrThrow().value,
        pApp = db.memory.keyedStableRefPointer(
            key = uniqueFunctionKey(name, nArg, encoding),
            data = CreateFunction(
                func = func,
                step = step,
                final = final
            ),
            userData = userData
        ),
        xFunc = CreateFunctionFuncHandler.handle(func),
        xStep = CreateFunctionStepHandler.handle(step),
        xFinal = CreateFunctionFinalHandler.handle(final)
    )
)

public actual fun sqlite3_create_function_v2(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    userData: sqlite3_mutable_pointer?,
    func: Sqlite3CreateFunctionFuncCallback?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_create_function_v2(
        db = db.pointer,
        zFunctionName = name,
        nArg = nArg,
        eTextRep = encoding.utf8OrThrow().value,
        pApp = db.memory.keyedStableRefPointer(
            key = uniqueFunctionKey(name, nArg, encoding),
            data = CreateFunction(
                func = func,
                step = step,
                final = final
            ),
            userData = userData
        ),
        xFunc = CreateFunctionFuncHandler.handle(func),
        xStep = CreateFunctionStepHandler.handle(step),
        xFinal = CreateFunctionFinalHandler.handle(final),
        xDestroy = stableRefDisposer(0, destructor)
    )
)

public actual fun sqlite3_create_module(
    db: sqlite3,
    name: String,
    module: sqlite3_module?,
    userData: sqlite3_mutable_pointer?
): Sqlite3Result = convertResult(
    native_sqlite3_create_module(
        db = db.pointer,
        zName = name,
        p = module?.pointer,
        pClientData = userData?.block?.pointer
    )
)

public actual fun sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module?,
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_create_module_v2(
        db = db.pointer,
        zName = name,
        p = module?.pointer,
        pClientData = userData?.block?.pointer,
        xDestroy = userDataDisposer(userData, destructor)
    )
)

public actual fun sqlite3_create_window_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    userData: sqlite3_mutable_pointer?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?,
    value: Sqlite3CreateFunctionValueCallback?,
    inverse: Sqlite3CreateFunctionInverseCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_create_window_function(
        db = db.pointer,
        zFunctionName = name,
        nArg = nArg,
        eTextRep = encoding.utf8OrThrow().value,
        pApp = db.memory.keyedStableRefPointer(
            key = uniqueFunctionKey(name, nArg, encoding),
            data = CreateFunction(
                step = step,
                final = final,
                value = value,
                inverse = inverse
            ),
            userData = userData
        ),
        xStep = CreateFunctionStepHandler.handle(step),
        xFinal = CreateFunctionFinalHandler.handle(final),
        xValue = CreateFunctionValueHandler.handle(value),
        xInverse = CreateFunctionInverseHandler.handle(inverse),
        xDestroy = stableRefDisposer(0, destructor)
    )
)

public actual fun sqlite3_data_count(stmt: sqlite3_stmt): Int =
    native_sqlite3_data_count(stmt.pointer)

public actual fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result =
    convertResult(native_sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: Sqlite3DbConfigOption,
): Sqlite3Result = commonSqlite3DbConfig(
    option = option,
    memoryPointer = { it.block.pointer },
    outParamConfig = {
        useParamMemScoped(state) { statePtr ->
            native_sqlite3_db_config(db.pointer, id, value, statePtr)
        }
    },
    nativeConfig = { id, values ->
        native_sqlite3_db_config(db.pointer, id, *values.toVariadicArguments())
    }
)

public actual fun sqlite3_db_filename(
    db: sqlite3,
    name: String
): sqlite3_filename? = native_sqlite3_db_filename(
    db = db.pointer,
    zDbName = name
)?.toKStringFromUtf8()

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    native_sqlite3_db_handle(stmt.pointer)?.let(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = native_sqlite3_db_name(
    db = db.pointer,
    N = index
)?.toKStringFromUtf8()

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    name: String
): Int = native_sqlite3_db_readonly(
    db = db.pointer,
    zDbName = name
)

public actual fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result =
    convertResult(native_sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Sqlite3IntOutParam?,
    outHighwater: Sqlite3IntOutParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native_sqlite3_db_status(
        arg0 = db.pointer,
        op = option.id,
        pCur = curPtr,
        pHiwtr = highPtr,
        resetFlg = resetFlag
    )
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Sqlite3LongOutParam?,
    outHighwater: Sqlite3LongOutParam?,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native_sqlite3_db_status64(
        arg0 = db.pointer,
        arg1 = option.id,
        arg2 = curPtr,
        arg3 = highPtr,
        arg4 = resetFlag
    )
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result = convertResult(
    native_sqlite3_declare_vtab(
        arg0 = db.pointer,
        zSQL = sql
    )
)

public actual fun sqlite3_deserialize(
    db: sqlite3,
    schema: String?,
    data: sqlite3_mutable_pointer?,
    dbSize: Long,
    dataSize: Long,
    flags: Sqlite3DeserializeFlag?
): Sqlite3Result = convertResult(
    native_sqlite3_deserialize(
        db = db.pointer,
        zSchema = schema,
        pData = data?.block?.pointer?.reinterpret(),
        szDb = dbSize,
        szBuf = dataSize,
        mFlags = flags?.value?.convert() ?: 0U
    )
)

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result = convertResult(memScoped {
    native_sqlite3_drop_modules(
        db = db.pointer,
        azKeep = keep?.toCStringArray(this)
    )
})

public actual fun sqlite3_errcode(db: sqlite3): Int =
    native_sqlite3_errcode(db.pointer)

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    native_sqlite3_errmsg(db.pointer)?.toKStringFromUtf8()

public actual fun sqlite3_errstr(resultCode: Int): String? =
    native_sqlite3_errstr(resultCode)?.toKStringFromUtf8()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    native_sqlite3_error_offset(db.pointer)

public actual fun sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Sqlite3Utf8OutParam?,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3ExecCallback?
): Sqlite3Result = convertResult(useMemoryManager {
    memScoped {
        useParam(outErrorMessage) { errorMessagePtr ->
            native_sqlite3_exec(
                arg0 = db.pointer,
                sql = sql.cstr.ptr,
                callback = ExecHandler.handle(callback),
                arg3 = stableRefPointer(callback, userData),
                errmsg = errorMessagePtr
            )
        }
    }
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = native_sqlite3_expanded_sql(stmt.pointer) ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    native_sqlite3_free(arg0 = pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): Int =
    native_sqlite3_extended_errcode(db.pointer)

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result = convertResult(
    native_sqlite3_extended_result_codes(
        arg0 = db.pointer,
        onoff = enabled
    )
)

public actual fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode,
    userData: sqlite3_mutable_pointer?
): Sqlite3Result = convertResult(
    native_sqlite3_file_control(
        arg0 = db.pointer,
        zDbName = name,
        op = opcode.code,
        arg3 = userData?.block?.pointer
    )
)

public actual fun sqlite3_finalize(stmt: sqlite3_stmt?): Sqlite3Result =
    stmt.deallocateNullable { native_sqlite3_finalize(stmt?.pointer) }

public actual fun sqlite3_free(data: sqlite3_mutable_pointer?): Unit =
    native_sqlite3_free(data?.block?.pointer)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    native_sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_get_auxdata(
    context: sqlite3_context,
    index: Int
): sqlite3_mutable_pointer? = native_sqlite3_get_auxdata(
    arg0 = context.pointer,
    N = index
)?.let { refPointer ->
    stableRefData<sqlite3_mutable_pointer>(refPointer).first
}

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    native_sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): Sqlite3Result =
    convertResult(native_sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    native_sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    native_sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_keyword_count(): Int =
    native_sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Sqlite3Utf8OutParam,
): Sqlite3Result = convertResult(memScoped {
    useParam(outName) { namePtr ->
        val size = Sqlite3IntOutParam(0)

        useParam(size) { sizePtr ->
            native_sqlite3_keyword_name(
                arg0 = index,
                arg1 = namePtr,
                arg2 = sizePtr
            )
        }.also {
            outName.size = size.value
        }
    }
})

public actual fun sqlite3_keyword_check(
    word: String,
    size: Int?
): Int = memScoped {
    val cWord = word.cstr

    native_sqlite3_keyword_check(
        arg0 = cWord,
        arg1 = size ?: cWord.size
    )
}

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
): Int = native_sqlite3_limit(
    arg0 = db.pointer,
    id = id.id,
    newVal = newVal
)

public actual fun sqlite3_log(
    errCode: Int,
    message: String
): Unit = native_sqlite3_log(
    iErrCode = errCode,
    zFormat = message
)

public actual fun sqlite3_malloc(size: Int): sqlite3_mutable_pointer? =
    native_sqlite3_malloc(size)?.let { sqlite3_mutable_pointer.from(it, size.toLong()) }

public actual fun sqlite3_malloc64(size: Long): sqlite3_mutable_pointer? =
    native_sqlite3_malloc64(size.toULong())?.let { sqlite3_mutable_pointer.from(it, size) }

public actual fun sqlite3_memory_used(): Long =
    native_sqlite3_memory_used()

public actual fun sqlite3_memory_highwater(resetFlag: Int): Long =
    native_sqlite3_memory_highwater(resetFlag)

public actual fun sqlite3_msize(data: sqlite3_mutable_pointer?): ULong =
    native_sqlite3_msize(data?.block?.pointer)

public actual fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt?
): sqlite3_stmt? = native_sqlite3_next_stmt(
    pDb = db.pointer,
    pStmt = stmt?.pointer
)?.let(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: Sqlite3DatabaseConnectionOutParam
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPtr ->
        native_sqlite3_open(
            filename = fileName.cstr.ptr,
            ppDb = dbPtr
        )
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: Sqlite3DatabaseConnectionOutParam,
    flags: Sqlite3FileOpenFlag.Valid,
    vfs: String?
): Sqlite3Result = convertResult(memScoped {
    useParam(outDb) { dbPtr ->
        native_sqlite3_open_v2(
            filename = fileName.cstr.ptr,
            ppDb = dbPtr,
            flags = flags.value,
            zVfs = vfs?.cstr?.ptr
        )
    }
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): Sqlite3Result = convertResult(
    native_sqlite3_overload_function(
        arg0 = db.pointer,
        zFuncName = name,
        nArg = nArg
    )
)

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    size: Int?,
    outStmt: Sqlite3StatementOutParam,
    outTail: Sqlite3Utf8OutParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outStmt, outTail) { stmtPtr, tailPtr ->
        val cSql = sql.cstr

        native_sqlite3_prepare_v2(
            db = db.pointer,
            zSql = cSql.ptr,
            nByte = size ?: cSql.size,
            ppStmt = stmtPtr,
            pzTail = tailPtr
        )
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    size: Int?,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StatementOutParam,
    outTail: Sqlite3Utf8OutParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outStmt, outTail) { stmtPtr, tailPtr ->
        val csql = sql.cstr

        native_sqlite3_prepare_v3(
            db = db.pointer,
            zSql = csql.ptr,
            nByte = size ?: csql.size,
            prepFlags = flags?.value?.convert() ?: 0U,
            ppStmt = stmtPtr,
            pzTail = tailPtr
        )
    }
})

public actual fun sqlite3_preupdate_blobwrite(db: sqlite3): Int =
    native_sqlite3_preupdate_blobwrite(db.pointer)

public actual fun sqlite3_preupdate_count(db: sqlite3): Int =
    native_sqlite3_preupdate_count(db.pointer)

public actual fun sqlite3_preupdate_depth(db: sqlite3): Int =
    native_sqlite3_preupdate_depth(db.pointer)

public actual fun sqlite3_preupdate_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3PreupdateHookCallback?
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.fromStableRef(
    native_sqlite3_preupdate_hook(
        db = db.pointer,
        xPreUpdate = PreupdateHookHandler.handle(callback),
        arg2 = db.memory.keyedStableRefPointer(KEY_PREUPDATE_HOOK, callback, userData)
    )
)

public actual fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutParam
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_preupdate_new(
        arg0 = db.pointer,
        arg1 = index,
        arg2 = valuePtr
    )
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutParam
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_preupdate_old(
        arg0 = db.pointer,
        arg1 = index,
        arg2 = valuePtr
    )
})

public actual fun sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3ProgressHandlerCallback?
): Unit = native_sqlite3_progress_handler(
    arg0 = db.pointer,
    arg1 = nOps,
    arg2 = ProgressHandlerHandler.handle(callback),
    arg3 = db.memory.keyedStableRefPointer(KEY_PROGRESS_HANDLER, callback, userData)
)

public actual fun sqlite3_randomness(
    size: Int,
    data: sqlite3_mutable_pointer?
): Unit = native_sqlite3_randomness(
    N = size,
    P = data?.block?.pointer
)

public actual fun sqlite3_realloc(
    data: sqlite3_mutable_pointer?,
    size: Int
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native_sqlite3_realloc(
        arg0 = data?.block?.pointer,
        arg1 = size
    ),
    size = size.toLong()
)

public actual fun sqlite3_realloc64(
    data: sqlite3_mutable_pointer?,
    size: Long
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.from(
    pointer = native_sqlite3_realloc64(
        arg0 = data?.block?.pointer,
        arg1 = size.convert()
    ),
    size = size
)

public actual fun sqlite3_release_memory(size: Int): Int =
    native_sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native_sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { native_sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    data: ByteArray?,
    size: Int,
    destructor: Sqlite3DestructorCallback?
): Unit = native_sqlite3_result_blob(
    arg0 = context.pointer,
    arg1 = context.db.memory.byteArrayPointer(data, destructor),
    arg2 = size,
    arg3 = globalDisposer(data)
)

public actual fun sqlite3_result_blob64(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    size: Long,
    destructor: Sqlite3DestructorCallback?
): Unit = native_sqlite3_result_blob64(
    arg0 = context.pointer,
    arg1 = data?.block?.pointer,
    arg2 = size.convert(),
    arg3 = userDataDisposer(data, destructor)
)

public actual fun sqlite3_result_double(
    context: sqlite3_context,
    value: Double
): Unit = native_sqlite3_result_double(
    arg0 = context.pointer,
    arg1 = value
)

public actual fun sqlite3_result_error(
    context: sqlite3_context,
    message: String?,
    size: Int?
): Unit = memScoped {
    val cMessage = message?.cstr

    native_sqlite3_result_error(
        arg0 = context.pointer,
        arg1 = cMessage?.ptr,
        arg2 = size ?: cMessage?.size ?: 0
    )
}

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    code: Int
): Unit = native_sqlite3_result_error_code(
    arg0 = context.pointer,
    arg1 = code
)

public actual fun sqlite3_result_error_nomem(context: sqlite3_context): Unit =
    native_sqlite3_result_error_nomem(context.pointer)

public actual fun sqlite3_result_error_toobig(context: sqlite3_context): Unit =
    native_sqlite3_result_error_toobig(context.pointer)

public actual fun sqlite3_result_int(
    context: sqlite3_context,
    value: Int
): Unit = native_sqlite3_result_int(
    arg0 = context.pointer,
    arg1 = value
)

public actual fun sqlite3_result_int64(
    context: sqlite3_context,
    value: Long
): Unit = native_sqlite3_result_int64(
    arg0 = context.pointer,
    arg1 = value
)

public actual fun sqlite3_result_null(context: sqlite3_context): Unit =
    native_sqlite3_result_null(context.pointer)

public actual fun sqlite3_result_pointer(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    type: String?,
    destructor: Sqlite3DestructorCallback?
): Unit = allocateNamedPointer(type, destructor) {
    native_sqlite3_result_pointer(
        arg0 = context.pointer,
        arg1 = context.db.memory.stableRefPointer(this, data, disposer),
        arg2 = typePointer,
        arg3 = stableRefDisposer(this, disposer)
    )
}

public actual fun sqlite3_result_subtype(
    context: sqlite3_context,
    subtype: UInt
): Unit = native_sqlite3_result_subtype(
    arg0 = context.pointer,
    arg1 = subtype
)

public actual fun sqlite3_result_text(
    context: sqlite3_context,
    text: String?,
    size: Int?
): Unit = memScoped {
    val cText = text?.cstr

    native_sqlite3_result_text(
        arg0 = context.pointer,
        arg1 = cText?.ptr,
        arg2 = size ?: cText?.size ?: 0,
        arg3 = SQLITE_TRANSIENT
    )
}

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    data: sqlite3_mutable_pointer?,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destructor: Sqlite3DestructorCallback?
): Unit = native_sqlite3_result_text64(
    arg0 = context.pointer,
    z = data?.block?.pointer,
    n = size.convert(),
    arg3 = userDataDisposer(data, destructor),
    encoding = encoding.utf8OrThrow().value.convert()
)

public actual fun sqlite3_result_value(
    context: sqlite3_context,
    value: sqlite3_value?,
): Unit = native_sqlite3_result_value(
    arg0 = context.pointer,
    arg1 = value?.pointer
)

public actual fun sqlite3_result_zeroblob(
    context: sqlite3_context,
    size: Int
): Unit = native_sqlite3_result_zeroblob(
    arg0 = context.pointer,
    n = size
)

public actual fun sqlite3_result_zeroblob64(
    context: sqlite3_context,
    size: ULong
): Int = native_sqlite3_result_zeroblob64(
    arg0 = context.pointer,
    n = size
)

public actual fun sqlite3_rollback_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3RollbackHookCallback?
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.fromStableRef(
    native_sqlite3_rollback_hook(
        arg0 = db.pointer,
        arg1 = RollbackHookHandler.handle(callback),
        arg2 = db.memory.keyedStableRefPointer(KEY_ROLLBACK_HOOK, callback, userData)
    )
)

public actual fun sqlite3_serialize(
    db: sqlite3,
    schema: String?,
    flags: Sqlite3SerializeFlag?
): sqlite3_mutable_pointer? {
    val size = Sqlite3LongOutParam(0)

    val pointer = memScoped {
        useParam(size) { sizePtr ->
            native_sqlite3_serialize(
                db = db.pointer,
                zSchema = schema?.cstr?.ptr,
                piSize = sizePtr,
                mFlags = flags?.value?.convert() ?: 0U
            )
        }
    }

    return sqlite3_mutable_pointer.from(pointer, size.value)
}

public actual fun sqlite3_set_authorizer(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3SetAuthorizerCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_set_authorizer(
        arg0 = db.pointer,
        xAuth = SetAuthorizerHandler.handle(callback),
        pUserData = db.memory.keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, userData)
    )
)

public actual fun sqlite3_set_auxdata(
    context: sqlite3_context,
    index: Int,
    data: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?
): Unit = native_sqlite3_set_auxdata(
    arg0 = context.pointer,
    N = index,
    arg2 = context.db.memory.keyedStableRefPointer(auxDataKey(index), data, data, destructor),
    arg3 = stableRefDisposer(data, destructor)
)

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: Sqlite3Result.Failure,
    message: String?
): Sqlite3Result = convertResult(
    native_sqlite3_set_errmsg(
        db = db.pointer,
        errcode = errorCode.code,
        zErrMsg = message
    )
)

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = native_sqlite3_set_last_insert_rowid(
    arg0 = db.pointer,
    arg1 = rowId
)

public actual fun sqlite3_shutdown(): Sqlite3Result =
    convertResult(native_sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = native_sqlite3_snapshot_cmp(
    p1 = snapshot1.pointer,
    p2 = snapshot2.pointer
)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    native_sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: Sqlite3SnapshotOutParam
): Sqlite3Result = convertResult(useParamMemScoped(outSnapshot) { snapshotPtr ->
    native_sqlite3_snapshot_get(
        db = db.pointer,
        zSchema = name,
        ppSnapshot = snapshotPtr
    )
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): Sqlite3Result = convertResult(
    native_sqlite3_snapshot_open(
        db = db.pointer,
        zSchema = name,
        pSnapshot = snapshot.pointer
    )
)

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(
    native_sqlite3_snapshot_recover(
        db = db.pointer,
        zDb = name
    )
)

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    native_sqlite3_soft_heap_limit64(limit)

public actual fun sqlite3_sourceid(): String =
    native_sqlite3_sourceid()!!.toKStringFromUtf8()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    native_sqlite3_sql(stmt.pointer)!!.toKStringFromUtf8()

public actual fun sqlite3_status(
    option: Sqlite3StatusOption,
    outCurrent: Sqlite3IntOutParam,
    outHighwater: Sqlite3IntOutParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native_sqlite3_status(
        op = option.id,
        pCurrent = curPtr,
        pHighwater = highPtr,
        resetFlag = resetFlag
    )
})

public actual fun sqlite3_status64(
    option: Sqlite3StatusOption,
    outCurrent: Sqlite3LongOutParam,
    outHighwater: Sqlite3LongOutParam,
    resetFlag: Int
): Sqlite3Result = convertResult(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
    native_sqlite3_status64(
        op = option.id,
        pCurrent = curPtr,
        pHighwater = highPtr,
        resetFlag = resetFlag
    )
})

public actual fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result =
    convertResult(native_sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    native_sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: Sqlite3ExplainMode
): Sqlite3Result = convertResult(
    native_sqlite3_stmt_explain(
        pStmt = stmt.pointer,
        eMode = mode.id
    )
)

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode =
    convertExplainMode(native_sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    native_sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: Sqlite3StatementStatusCounter,
    resetFlag: Int
): Int = native_sqlite3_stmt_status(
    arg0 = stmt.pointer,
    op = counter.id,
    resetFlg = resetFlag
)

public actual fun sqlite3_strglob(
    pattern: String,
    string: String
): Int = native_sqlite3_strglob(
    zGlob = pattern,
    zStr = string
)

public actual fun sqlite3_stricmp(
    left: String,
    right: String
): Int = native_sqlite3_stricmp(
    arg0 = left,
    arg1 = right
)

public actual fun sqlite3_strlike(
    pattern: String,
    string: String,
    escape: Char
): Int = native_sqlite3_strlike(
    zGlob = pattern,
    zStr = string,
    cEsc = escape.code.convert()
)

public actual fun sqlite3_strnicmp(
    left: String,
    right: String,
    size: Int
): Int = native_sqlite3_strnicmp(
    arg0 = left,
    arg1 = right,
    arg2 = size
)

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    native_sqlite3_system_errno(db.pointer)

public actual fun sqlite3_table_column_metadata(
    db: sqlite3,
    dbName: String?,
    tableName: String,
    columnName: String,
    outDataType: Sqlite3Utf8OutParam?,
    outCollationName: Sqlite3Utf8OutParam?,
    outNotNull: Sqlite3IntOutParam?,
    outPrimaryKey: Sqlite3IntOutParam?,
    outAutoIncrement: Sqlite3IntOutParam?
): Sqlite3Result = convertResult(memScoped {
    val dataTypePtr = outDataType?.attach(this)
    val collationNamePtr = outCollationName?.attach(this)
    val notNullPtr = outNotNull?.attach(this)
    val primaryKeyPtr = outPrimaryKey?.attach(this)
    val autoIncrementPtr = outAutoIncrement?.attach(this)

    try {
        native_sqlite3_table_column_metadata(
            db = db.pointer,
            zDbName = dbName?.cstr?.ptr,
            zTableName = tableName.cstr.ptr,
            zColumnName = columnName.cstr.ptr,
            pzDataType = dataTypePtr,
            pzCollSeq = collationNamePtr,
            pNotNull = notNullPtr,
            pPrimaryKey = primaryKeyPtr,
            pAutoinc = autoIncrementPtr
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

public actual fun sqlite3_trace_v2(
    db: sqlite3,
    mask: Sqlite3TraceCode?,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3TraceCallback?
): Sqlite3Result = convertResult(
    native_sqlite3_trace_v2(
        arg0 = db.pointer,
        uMask = mask?.value?.convert() ?: 0,
        xCallback = TraceHandler.handle(callback),
        pCtx = db.memory.keyedStableRefPointer(KEY_TRACE, callback, userData)
    )
)

public actual fun sqlite3_txn_state(
    db: sqlite3,
    schema: String?
): Sqlite3TransactionState? = convertTransactionState(
    native_sqlite3_txn_state(
        arg0 = db.pointer,
        zSchema = schema
    )
)

public actual fun sqlite3_update_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3UpdateHookCallback?
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.fromStableRef(
    native_sqlite3_update_hook(
        arg0 = db.pointer,
        arg1 = UpdateHookHandler.handle(callback),
        arg2 = db.memory.keyedStableRefPointer(KEY_UPDATE_HOOK, callback, userData)
    )
)

public actual fun sqlite3_uri_boolean(
    fileName: sqlite3_filename,
    parameter: String,
    default: Int
): Int = native_sqlite3_uri_boolean(
    z = fileName,
    zParam = parameter,
    bDefault = default
)

public actual fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long = native_sqlite3_uri_int64(
    arg0 = fileName,
    arg1 = parameter,
    arg2 = default
)

public actual fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String? = native_sqlite3_uri_key(
    z = fileName,
    N = index
)?.toKStringFromUtf8()

public actual fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String? = native_sqlite3_uri_parameter(
    z = fileName,
    zParam = parameter
)?.toKStringFromUtf8()

public actual fun sqlite3_user_data(context: sqlite3_context): sqlite3_mutable_pointer? =
    stableRefData<CreateFunction>(native_sqlite3_user_data(context.pointer) ?: return null).second

public actual fun sqlite3_value_bytes(value: sqlite3_value): Int =
    native_sqlite3_value_bytes(value.pointer)

public actual fun sqlite3_value_double(value: sqlite3_value): Double =
    native_sqlite3_value_double(value.pointer)

public actual fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value? =
    native_sqlite3_value_dup(value.pointer)?.let(::sqlite3_value)

public actual fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2? =
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

public actual fun sqlite3_value_pointer(
    value: sqlite3_value,
    type: String?
): sqlite3_mutable_pointer? = stableRefData<NamedPointer>(
    native_sqlite3_value_pointer(
        arg0 = value.pointer,
        arg1 = type
    ) ?: return null
).second

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    native_sqlite3_value_subtype(value.pointer)

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    native_sqlite3_value_text(value.pointer)?.reinterpret<ByteVar>()?.toKStringFromUtf8()

public actual fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType =
    convertDataType(native_sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? =
    native_sqlite3_vfs_find(name)?.let(::sqlite3_vfs)

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): Sqlite3Result = convertResult(
    native_sqlite3_vfs_register(
        arg0 = vfs.pointer,
        makeDflt = makeDefault
    )
)

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): Sqlite3Result =
    convertResult(native_sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String? = native_sqlite3_vtab_collation(
    arg0 = info.pointer,
    arg1 = index
)?.toKStringFromUtf8()

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: Sqlite3VirtualTableConfigOption
): Sqlite3Result = commonSqlite3VtabConfig(option){ id, values ->
    native_sqlite3_vtab_config(db.pointer, id, *values.toVariadicArguments())
}

public actual fun sqlite3_vtab_distinct(info: sqlite3_index_info): Int =
    native_sqlite3_vtab_distinct(info.pointer)

public actual fun sqlite3_vtab_in(
    info: sqlite3_index_info,
    index: Int,
    handle: Int
): Int = native_sqlite3_vtab_in(
    arg0 = info.pointer,
    iCons = index,
    bHandle = handle
)

public actual fun sqlite3_vtab_in_first(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_in_first(
        pVal = value.pointer,
        ppOut = valuePtr
    )
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_in_next(
        pVal = value.pointer,
        ppOut = valuePtr
    )
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    native_sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): Sqlite3Result =
    convertResult(native_sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: Sqlite3ValueOutParam?
): Sqlite3Result = convertResult(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_rhs_value(
        arg0 = info.pointer,
        arg1 = index,
        ppVal = valuePtr
    )
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): Sqlite3Result = convertResult(
    native_sqlite3_wal_autocheckpoint(
        db = db.pointer,
        N = nFrame
    )
)

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    name: String?
): Sqlite3Result = convertResult(
    native_sqlite3_wal_checkpoint(
        db = db.pointer,
        zDb = name
    )
)

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    name: String?,
    mode: Sqlite3CheckpointMode,
    outNLog: Sqlite3IntOutParam?,
    outNCkpt: Sqlite3IntOutParam?
): Sqlite3Result = convertResult(memScoped {
    useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
        native_sqlite3_wal_checkpoint_v2(
            db = db.pointer,
            zDb = name?.cstr?.ptr,
            eMode = mode.id,
            pnLog = nLogPtr,
            pnCkpt = nCkptPtr
        )
    }
})

public actual fun sqlite3_wal_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3WalHookCallback?
): sqlite3_mutable_pointer? = sqlite3_mutable_pointer.fromStableRef(
    native_sqlite3_wal_hook(
        arg0 = db.pointer,
        arg1 = WalHookHandler.handle(callback),
        arg2 = db.memory.keyedStableRefPointer(KEY_WAL_HOOK, callback, userData)
    )
)