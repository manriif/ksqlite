@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toCStringArray
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.capi.callbacks.SqliteAutoExtensionCallback
import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.callbacks.SqliteExecCallback
import ksqlite.capi.callbacks.SqliteFunctionFinalCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.callbacks.SqliteFunctionInverseCallback
import ksqlite.capi.callbacks.SqliteFunctionStepCallback
import ksqlite.capi.callbacks.SqliteFunctionValueCallback
import ksqlite.capi.callbacks.SqlitePreupdateHookCallback
import ksqlite.capi.callbacks.SqliteProgressHandlerCallback
import ksqlite.capi.callbacks.SqliteRollbackHookCallback
import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.callbacks.SqliteUpdateHookCallback
import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.handlers.AuthorizerHandler
import ksqlite.capi.handlers.AutovacuumPagesHandler
import ksqlite.capi.handlers.BusyHandlerHandler
import ksqlite.capi.handlers.CollationHandler
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
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.OpaqueBuffer
import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.capi.memory.bufferDisposer
import ksqlite.capi.memory.contentSize
import ksqlite.capi.memory.copyBytes
import ksqlite.capi.memory.globalDisposer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.overriding
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toVariadicArguments
import ksqlite.capi.memory.useMemoryManager
import ksqlite.capi.memory.useParam
import ksqlite.capi.memory.useParamMemScoped
import ksqlite.capi.memory.useParams
import ksqlite.capi.memory.useParamsMemScoped
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.capi.types.SqliteFileControlOpcode
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.capi.vtab.SqliteVtabConfigOption
import ksqlite.capi.vtab.createVtabModule
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.foreign.KSQLITE_TRANSIENT
import ksqlite.foreign.ksqlite_auto_extension
import ksqlite.foreign.ksqlite_cancel_auto_extension
import ksqlite.foreign.ksqlite_prepare_v2
import ksqlite.foreign.ksqlite_prepare_v3
import ksqlite.types.SqliteBlobOpenFlag
import ksqlite.types.SqliteCheckpointMode
import ksqlite.types.SqliteCompleteResult
import ksqlite.types.SqliteConflictResolutionMode
import ksqlite.types.SqliteDataType
import ksqlite.types.SqliteDbReadonlyResult
import ksqlite.types.SqliteDbStatusOption
import ksqlite.types.SqliteDeserializeFlag
import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqliteFunctionTextEncoding
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqlitePrepareFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.SqliteRuntimeLimit
import ksqlite.types.SqliteStatementStatusCounter
import ksqlite.types.SqliteStatusOption
import ksqlite.types.SqliteTextEncoding
import ksqlite.types.SqliteTraceEventCode
import ksqlite.types.SqliteTransactionState
import ksqlite.types.internal.convertCompleteResult
import ksqlite.types.internal.convertConflictResolutionMode
import ksqlite.types.internal.convertDataType
import ksqlite.types.internal.convertDbReadonlyResult
import ksqlite.types.internal.convertExplainMode
import ksqlite.types.internal.convertResultCode
import ksqlite.types.internal.convertTextEncoding
import ksqlite.types.internal.convertTransactionState
import ksqlite.foreign.sqlite3_autovacuum_pages as native_sqlite3_autovacuum_pages
import ksqlite.foreign.sqlite3_backup_finish as native_sqlite3_backup_finish
import ksqlite.foreign.sqlite3_backup_init as native_sqlite3_backup_init
import ksqlite.foreign.sqlite3_backup_pagecount as native_sqlite3_backup_pagecount
import ksqlite.foreign.sqlite3_backup_remaining as native_sqlite3_backup_remaining
import ksqlite.foreign.sqlite3_backup_step as native_sqlite3_backup_step
import ksqlite.foreign.sqlite3_bind_blob as native_sqlite3_bind_blob
import ksqlite.foreign.sqlite3_bind_blob64 as native_sqlite3_bind_blob64
import ksqlite.foreign.sqlite3_bind_double as native_sqlite3_bind_double
import ksqlite.foreign.sqlite3_bind_int as native_sqlite3_bind_int
import ksqlite.foreign.sqlite3_bind_int64 as native_sqlite3_bind_int64
import ksqlite.foreign.sqlite3_bind_null as native_sqlite3_bind_null
import ksqlite.foreign.sqlite3_bind_parameter_count as native_sqlite3_bind_parameter_count
import ksqlite.foreign.sqlite3_bind_parameter_index as native_sqlite3_bind_parameter_index
import ksqlite.foreign.sqlite3_bind_parameter_name as native_sqlite3_bind_parameter_name
import ksqlite.foreign.sqlite3_bind_pointer as native_sqlite3_bind_pointer
import ksqlite.foreign.sqlite3_bind_text as native_sqlite3_bind_text
import ksqlite.foreign.sqlite3_bind_text64 as native_sqlite3_bind_text64
import ksqlite.foreign.sqlite3_bind_value as native_sqlite3_bind_value
import ksqlite.foreign.sqlite3_bind_zeroblob as native_sqlite3_bind_zeroblob
import ksqlite.foreign.sqlite3_bind_zeroblob64 as native_sqlite3_bind_zeroblob64
import ksqlite.foreign.sqlite3_blob_bytes as native_sqlite3_blob_bytes
import ksqlite.foreign.sqlite3_blob_close as native_sqlite3_blob_close
import ksqlite.foreign.sqlite3_blob_open as native_sqlite3_blob_open
import ksqlite.foreign.sqlite3_blob_read as native_sqlite3_blob_read
import ksqlite.foreign.sqlite3_blob_reopen as native_sqlite3_blob_reopen
import ksqlite.foreign.sqlite3_blob_write as native_sqlite3_blob_write
import ksqlite.foreign.sqlite3_busy_handler as native_sqlite3_busy_handler
import ksqlite.foreign.sqlite3_busy_timeout as native_sqlite3_busy_timeout
import ksqlite.foreign.sqlite3_changes as native_sqlite3_changes
import ksqlite.foreign.sqlite3_changes64 as native_sqlite3_changes64
import ksqlite.foreign.sqlite3_clear_bindings as native_sqlite3_clear_bindings
import ksqlite.foreign.sqlite3_close as native_sqlite3_close
import ksqlite.foreign.sqlite3_close_v2 as native_sqlite3_close_v2
import ksqlite.foreign.sqlite3_collation_needed as native_sqlite3_collation_needed
import ksqlite.foreign.sqlite3_column_blob as native_sqlite3_column_blob
import ksqlite.foreign.sqlite3_column_bytes as native_sqlite3_column_bytes
import ksqlite.foreign.sqlite3_column_count as native_sqlite3_column_count
import ksqlite.foreign.sqlite3_column_database_name as native_sqlite3_column_database_name
import ksqlite.foreign.sqlite3_column_decltype as native_sqlite3_column_decltype
import ksqlite.foreign.sqlite3_column_double as native_sqlite3_column_double
import ksqlite.foreign.sqlite3_column_int as native_sqlite3_column_int
import ksqlite.foreign.sqlite3_column_int64 as native_sqlite3_column_int64
import ksqlite.foreign.sqlite3_column_name as native_sqlite3_column_name
import ksqlite.foreign.sqlite3_column_origin_name as native_sqlite3_column_origin_name
import ksqlite.foreign.sqlite3_column_table_name as native_sqlite3_column_table_name
import ksqlite.foreign.sqlite3_column_text as native_sqlite3_column_text
import ksqlite.foreign.sqlite3_column_type as native_sqlite3_column_type
import ksqlite.foreign.sqlite3_column_value as native_sqlite3_column_value
import ksqlite.foreign.sqlite3_commit_hook as native_sqlite3_commit_hook
import ksqlite.foreign.sqlite3_compileoption_get as native_sqlite3_compileoption_get
import ksqlite.foreign.sqlite3_compileoption_used as native_sqlite3_compileoption_used
import ksqlite.foreign.sqlite3_complete as native_sqlite3_complete
import ksqlite.foreign.sqlite3_config as native_sqlite3_config
import ksqlite.foreign.sqlite3_context_db_handle as native_sqlite3_context_db_handle
import ksqlite.foreign.sqlite3_create_collation_v2 as native_sqlite3_create_collation_v2
import ksqlite.foreign.sqlite3_create_function_v2 as native_sqlite3_create_function_v2
import ksqlite.foreign.sqlite3_create_module_v2 as native_sqlite3_create_module_v2
import ksqlite.foreign.sqlite3_create_window_function as native_sqlite3_create_window_function
import ksqlite.foreign.sqlite3_data_count as native_sqlite3_data_count
import ksqlite.foreign.sqlite3_db_cacheflush as native_sqlite3_db_cacheflush
import ksqlite.foreign.sqlite3_db_config as native_sqlite3_db_config
import ksqlite.foreign.sqlite3_db_filename as native_sqlite3_db_filename
import ksqlite.foreign.sqlite3_db_handle as native_sqlite3_db_handle
import ksqlite.foreign.sqlite3_db_name as native_sqlite3_db_name
import ksqlite.foreign.sqlite3_db_readonly as native_sqlite3_db_readonly
import ksqlite.foreign.sqlite3_db_release_memory as native_sqlite3_db_release_memory
import ksqlite.foreign.sqlite3_db_status as native_sqlite3_db_status
import ksqlite.foreign.sqlite3_db_status64 as native_sqlite3_db_status64
import ksqlite.foreign.sqlite3_declare_vtab as native_sqlite3_declare_vtab
import ksqlite.foreign.sqlite3_deserialize as native_sqlite3_deserialize
import ksqlite.foreign.sqlite3_drop_modules as native_sqlite3_drop_modules
import ksqlite.foreign.sqlite3_errcode as native_sqlite3_errcode
import ksqlite.foreign.sqlite3_errmsg as native_sqlite3_errmsg
import ksqlite.foreign.sqlite3_error_offset as native_sqlite3_error_offset
import ksqlite.foreign.sqlite3_errstr as native_sqlite3_errstr
import ksqlite.foreign.sqlite3_exec as native_sqlite3_exec
import ksqlite.foreign.sqlite3_expanded_sql as native_sqlite3_expanded_sql
import ksqlite.foreign.sqlite3_extended_errcode as native_sqlite3_extended_errcode
import ksqlite.foreign.sqlite3_extended_result_codes as native_sqlite3_extended_result_codes
import ksqlite.foreign.sqlite3_file_control as native_sqlite3_file_control
import ksqlite.foreign.sqlite3_finalize as native_sqlite3_finalize
import ksqlite.foreign.sqlite3_free as native_sqlite3_free
import ksqlite.foreign.sqlite3_get_autocommit as native_sqlite3_get_autocommit
import ksqlite.foreign.sqlite3_hard_heap_limit64 as native_sqlite3_hard_heap_limit64
import ksqlite.foreign.sqlite3_initialize as native_sqlite3_initialize
import ksqlite.foreign.sqlite3_interrupt as native_sqlite3_interrupt
import ksqlite.foreign.sqlite3_is_interrupted as native_sqlite3_is_interrupted
import ksqlite.foreign.sqlite3_key as native_sqlite3_key
import ksqlite.foreign.sqlite3_key_v2 as native_sqlite3_key_v2
import ksqlite.foreign.sqlite3_keyword_check as native_sqlite3_keyword_check
import ksqlite.foreign.sqlite3_keyword_count as native_sqlite3_keyword_count
import ksqlite.foreign.sqlite3_keyword_name as native_sqlite3_keyword_name
import ksqlite.foreign.sqlite3_last_insert_rowid as native_sqlite3_last_insert_rowid
import ksqlite.foreign.sqlite3_libversion as native_sqlite3_libversion
import ksqlite.foreign.sqlite3_libversion_number as native_sqlite3_libversion_number
import ksqlite.foreign.sqlite3_limit as native_sqlite3_limit
import ksqlite.foreign.sqlite3_log as native_sqlite3_log
import ksqlite.foreign.sqlite3_malloc as native_sqlite3_malloc
import ksqlite.foreign.sqlite3_malloc64 as native_sqlite3_malloc64
import ksqlite.foreign.sqlite3_memory_highwater as native_sqlite3_memory_highwater
import ksqlite.foreign.sqlite3_memory_used as native_sqlite3_memory_used
import ksqlite.foreign.sqlite3_msize as native_sqlite3_msize
import ksqlite.foreign.sqlite3_next_stmt as native_sqlite3_next_stmt
import ksqlite.foreign.sqlite3_open as native_sqlite3_open
import ksqlite.foreign.sqlite3_open_v2 as native_sqlite3_open_v2
import ksqlite.foreign.sqlite3_overload_function as native_sqlite3_overload_function
import ksqlite.foreign.sqlite3_prepare_v2 as native_sqlite3_prepare_v2
import ksqlite.foreign.sqlite3_prepare_v3 as native_sqlite3_prepare_v3
import ksqlite.foreign.sqlite3_preupdate_blobwrite as native_sqlite3_preupdate_blobwrite
import ksqlite.foreign.sqlite3_preupdate_count as native_sqlite3_preupdate_count
import ksqlite.foreign.sqlite3_preupdate_depth as native_sqlite3_preupdate_depth
import ksqlite.foreign.sqlite3_preupdate_hook as native_sqlite3_preupdate_hook
import ksqlite.foreign.sqlite3_preupdate_new as native_sqlite3_preupdate_new
import ksqlite.foreign.sqlite3_preupdate_old as native_sqlite3_preupdate_old
import ksqlite.foreign.sqlite3_progress_handler as native_sqlite3_progress_handler
import ksqlite.foreign.sqlite3_randomness as native_sqlite3_randomness
import ksqlite.foreign.sqlite3_realloc as native_sqlite3_realloc
import ksqlite.foreign.sqlite3_realloc64 as native_sqlite3_realloc64
import ksqlite.foreign.sqlite3_rekey as native_sqlite3_rekey
import ksqlite.foreign.sqlite3_rekey_v2 as native_sqlite3_rekey_v2
import ksqlite.foreign.sqlite3_release_memory as native_sqlite3_release_memory
import ksqlite.foreign.sqlite3_reset as native_sqlite3_reset
import ksqlite.foreign.sqlite3_reset_auto_extension as native_sqlite3_reset_auto_extension
import ksqlite.foreign.sqlite3_result_blob as native_sqlite3_result_blob
import ksqlite.foreign.sqlite3_result_blob64 as native_sqlite3_result_blob64
import ksqlite.foreign.sqlite3_result_double as native_sqlite3_result_double
import ksqlite.foreign.sqlite3_result_error as native_sqlite3_result_error
import ksqlite.foreign.sqlite3_result_error_code as native_sqlite3_result_error_code
import ksqlite.foreign.sqlite3_result_error_nomem as native_sqlite3_result_error_nomem
import ksqlite.foreign.sqlite3_result_error_toobig as native_sqlite3_result_error_toobig
import ksqlite.foreign.sqlite3_result_int as native_sqlite3_result_int
import ksqlite.foreign.sqlite3_result_int64 as native_sqlite3_result_int64
import ksqlite.foreign.sqlite3_result_null as native_sqlite3_result_null
import ksqlite.foreign.sqlite3_result_pointer as native_sqlite3_result_pointer
import ksqlite.foreign.sqlite3_result_subtype as native_sqlite3_result_subtype
import ksqlite.foreign.sqlite3_result_text as native_sqlite3_result_text
import ksqlite.foreign.sqlite3_result_text64 as native_sqlite3_result_text64
import ksqlite.foreign.sqlite3_result_value as native_sqlite3_result_value
import ksqlite.foreign.sqlite3_result_zeroblob as native_sqlite3_result_zeroblob
import ksqlite.foreign.sqlite3_result_zeroblob64 as native_sqlite3_result_zeroblob64
import ksqlite.foreign.sqlite3_rollback_hook as native_sqlite3_rollback_hook
import ksqlite.foreign.sqlite3_set_authorizer as native_sqlite3_set_authorizer
import ksqlite.foreign.sqlite3_set_errmsg as native_sqlite3_set_errmsg
import ksqlite.foreign.sqlite3_set_last_insert_rowid as native_sqlite3_set_last_insert_rowid
import ksqlite.foreign.sqlite3_shutdown as native_sqlite3_shutdown
import ksqlite.foreign.sqlite3_snapshot_cmp as native_sqlite3_snapshot_cmp
import ksqlite.foreign.sqlite3_snapshot_free as native_sqlite3_snapshot_free
import ksqlite.foreign.sqlite3_snapshot_get as native_sqlite3_snapshot_get
import ksqlite.foreign.sqlite3_snapshot_open as native_sqlite3_snapshot_open
import ksqlite.foreign.sqlite3_snapshot_recover as native_sqlite3_snapshot_recover
import ksqlite.foreign.sqlite3_soft_heap_limit64 as native_sqlite3_soft_heap_limit64
import ksqlite.foreign.sqlite3_sourceid as native_sqlite3_sourceid
import ksqlite.foreign.sqlite3_sql as native_sqlite3_sql
import ksqlite.foreign.sqlite3_status as native_sqlite3_status
import ksqlite.foreign.sqlite3_status64 as native_sqlite3_status64
import ksqlite.foreign.sqlite3_step as native_sqlite3_step
import ksqlite.foreign.sqlite3_stmt_busy as native_sqlite3_stmt_busy
import ksqlite.foreign.sqlite3_stmt_explain as native_sqlite3_stmt_explain
import ksqlite.foreign.sqlite3_stmt_isexplain as native_sqlite3_stmt_isexplain
import ksqlite.foreign.sqlite3_stmt_readonly as native_sqlite3_stmt_readonly
import ksqlite.foreign.sqlite3_stmt_status as native_sqlite3_stmt_status
import ksqlite.foreign.sqlite3_strglob as native_sqlite3_strglob
import ksqlite.foreign.sqlite3_stricmp as native_sqlite3_stricmp
import ksqlite.foreign.sqlite3_strlike as native_sqlite3_strlike
import ksqlite.foreign.sqlite3_strnicmp as native_sqlite3_strnicmp
import ksqlite.foreign.sqlite3_system_errno as native_sqlite3_system_errno
import ksqlite.foreign.sqlite3_table_column_metadata as native_sqlite3_table_column_metadata
import ksqlite.foreign.sqlite3_threadsafe as native_sqlite3_threadsafe
import ksqlite.foreign.sqlite3_total_changes as native_sqlite3_total_changes
import ksqlite.foreign.sqlite3_total_changes64 as native_sqlite3_total_changes64
import ksqlite.foreign.sqlite3_trace_v2 as native_sqlite3_trace_v2
import ksqlite.foreign.sqlite3_txn_state as native_sqlite3_txn_state
import ksqlite.foreign.sqlite3_update_hook as native_sqlite3_update_hook
import ksqlite.foreign.sqlite3_uri_boolean as native_sqlite3_uri_boolean
import ksqlite.foreign.sqlite3_uri_int64 as native_sqlite3_uri_int64
import ksqlite.foreign.sqlite3_uri_key as native_sqlite3_uri_key
import ksqlite.foreign.sqlite3_uri_parameter as native_sqlite3_uri_parameter
import ksqlite.foreign.sqlite3_value_blob as native_sqlite3_value_blob
import ksqlite.foreign.sqlite3_value_bytes as native_sqlite3_value_bytes
import ksqlite.foreign.sqlite3_value_double as native_sqlite3_value_double
import ksqlite.foreign.sqlite3_value_dup as native_sqlite3_value_dup
import ksqlite.foreign.sqlite3_value_encoding as native_sqlite3_value_encoding
import ksqlite.foreign.sqlite3_value_free as native_sqlite3_value_free
import ksqlite.foreign.sqlite3_value_frombind as native_sqlite3_value_frombind
import ksqlite.foreign.sqlite3_value_int as native_sqlite3_value_int
import ksqlite.foreign.sqlite3_value_int64 as native_sqlite3_value_int64
import ksqlite.foreign.sqlite3_value_nochange as native_sqlite3_value_nochange
import ksqlite.foreign.sqlite3_value_numeric_type as native_sqlite3_value_numeric_type
import ksqlite.foreign.sqlite3_value_subtype as native_sqlite3_value_subtype
import ksqlite.foreign.sqlite3_value_text as native_sqlite3_value_text
import ksqlite.foreign.sqlite3_value_type as native_sqlite3_value_type
import ksqlite.foreign.sqlite3_vfs_find as native_sqlite3_vfs_find
import ksqlite.foreign.sqlite3_vfs_register as native_sqlite3_vfs_register
import ksqlite.foreign.sqlite3_vfs_unregister as native_sqlite3_vfs_unregister
import ksqlite.foreign.sqlite3_vtab_collation as native_sqlite3_vtab_collation
import ksqlite.foreign.sqlite3_vtab_config as native_sqlite3_vtab_config
import ksqlite.foreign.sqlite3_vtab_distinct as native_sqlite3_vtab_distinct
import ksqlite.foreign.sqlite3_vtab_in as native_sqlite3_vtab_in
import ksqlite.foreign.sqlite3_vtab_in_first as native_sqlite3_vtab_in_first
import ksqlite.foreign.sqlite3_vtab_in_next as native_sqlite3_vtab_in_next
import ksqlite.foreign.sqlite3_vtab_nochange as native_sqlite3_vtab_nochange
import ksqlite.foreign.sqlite3_vtab_on_conflict as native_sqlite3_vtab_on_conflict
import ksqlite.foreign.sqlite3_vtab_rhs_value as native_sqlite3_vtab_rhs_value
import ksqlite.foreign.sqlite3_wal_autocheckpoint as native_sqlite3_wal_autocheckpoint
import ksqlite.foreign.sqlite3_wal_checkpoint as native_sqlite3_wal_checkpoint
import ksqlite.foreign.sqlite3_wal_checkpoint_v2 as native_sqlite3_wal_checkpoint_v2
import ksqlite.foreign.sqlite3_wal_hook as native_sqlite3_wal_hook

public actual fun sqlite3_auto_extension(callback: SqliteAutoExtensionCallback): SqliteResultCode =
    autoExtensionRegister(callback) { ksqlite_auto_extension(AutoExtensionHandler) }

public actual fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?,
    callback: SqliteAutovacuumPagesCallback<in AppData>?
): SqliteResultCode = convertResultCode(
    native_sqlite3_autovacuum_pages(
        db.pointer,
        callbackHandler(callback, AutovacuumPagesHandler),
        db.memory.keyedStableRefPointer(KEY_AUTOVACUUM_PAGES, callback, appData, destroy),
        stableRefDisposer(callback, destroy)
    )
)

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): SqliteResultCode =
    convertResultCode(native_sqlite3_backup_finish(backup.pointer))

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
): SqliteResultCode = convertResultCode(native_sqlite3_backup_step(backup.pointer, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: SqliteDestroyCallback<ByteArray>?
): SqliteResultCode = convertResultCode(
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
    destroy: SqliteDestroyCallback<Buffer>?
): SqliteResultCode = convertResultCode(
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
): SqliteResultCode = convertResultCode(native_sqlite3_bind_double(stmt.pointer, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): SqliteResultCode = convertResultCode(native_sqlite3_bind_int(stmt.pointer, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): SqliteResultCode = convertResultCode(native_sqlite3_bind_int64(stmt.pointer, index, value))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): SqliteResultCode = convertResultCode(native_sqlite3_bind_null(stmt.pointer, index))

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
    destroy: SqliteDestroyCallback<Data>?
): SqliteResultCode = convertResultCode(allocateNamedPointer(type, destroy) { ptr, ptrDestroy ->
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
): SqliteResultCode = convertResultCode(memScoped {
    val cText = value.cstr
    native_sqlite3_bind_text(stmt.pointer, index, cText.ptr, cText.contentSize, KSQLITE_TRANSIENT)
})

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: SqliteTextEncoding.BindText,
    destroy: SqliteDestroyCallback<Buffer>?
): SqliteResultCode = convertResultCode(
    native_sqlite3_bind_text64(
        stmt.pointer,
        index,
        buffer.pointer,
        size.convert(),
        bufferDisposer(buffer, destroy),
        encoding.value.convert()
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): SqliteResultCode =
    convertResultCode(native_sqlite3_bind_value(stmt.pointer, index, value.pointer))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): SqliteResultCode = convertResultCode(native_sqlite3_bind_zeroblob(stmt.pointer, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): SqliteResultCode = convertResultCode(native_sqlite3_bind_zeroblob64(stmt.pointer, index, size))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    native_sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): SqliteResultCode =
    convertResultCode(native_sqlite3_blob_close(blob.pointer))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    database: String,
    tableName: String,
    columnName: String,
    rowid: Long,
    flags: SqliteBlobOpenFlag,
    outBlob: sqlite3_blob.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outBlob) { blobPtr ->
        native_sqlite3_blob_open(
            db.pointer,
            database.cstr.ptr,
            tableName.cstr.ptr,
            columnName.cstr.ptr,
            rowid,
            flags.value,
            blobPtr
        )
    }
})

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    output: ByteArray,
    size: Int,
    offset: Int
): SqliteResultCode =
    convertResultCode(native_sqlite3_blob_read(blob.pointer, output.refTo(0), size, offset))

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowid: Long
): SqliteResultCode = convertResultCode(native_sqlite3_blob_reopen(blob.pointer, rowid))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    input: ByteArray,
    size: Int,
    offset: Int
): SqliteResultCode =
    convertResultCode(native_sqlite3_blob_write(blob.pointer, input.refTo(0), size, offset))

public actual fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: SqliteBusyHandlerCallback<AppData>?
): SqliteResultCode = convertResultCode(
    native_sqlite3_busy_handler(
        db.pointer,
        callbackHandler(callback, BusyHandlerHandler),
        db.memory.keyedStableRefPointer(KEY_BUSY_HANDLER, callback, appData)
    )
)

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): SqliteResultCode = convertResultCode(native_sqlite3_busy_timeout(db.pointer, millis))

public actual fun sqlite3_cancel_auto_extension(callback: SqliteAutoExtensionCallback): Int =
    autoExtensionUnregister(callback) { ksqlite_cancel_auto_extension(AutoExtensionHandler) }

public actual fun sqlite3_changes(db: sqlite3): Int =
    native_sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    native_sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): SqliteResultCode =
    commonClearBindings(stmt, native_sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3): SqliteResultCode =
    db.deallocate { native_sqlite3_close(it.pointer) }

public actual fun sqlite3_close_v2(db: sqlite3): SqliteResultCode =
    db.deallocate { native_sqlite3_close_v2(it.pointer) }

public actual fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: SqliteCollationNeededCallback<AppData>?,
): SqliteResultCode = convertResultCode(
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
): SqliteDataType = convertDataType(native_sqlite3_column_type(stmt.pointer, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = native_sqlite3_column_value(stmt.pointer, index)
    ?.let(::sqlite3_value)

public actual fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteCommitHookCallback<AppData>?
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

public actual fun sqlite3_complete(sql: String): SqliteCompleteResult =
    convertCompleteResult(native_sqlite3_complete(sql))

public actual fun sqlite3_config(option: SqliteConfigOption): SqliteResultCode = commonConfig(
    option = option,
    logFunctionPointer = { cb, _ -> callbackHandler(cb, ConfigLogHandler) },
    sqllogFunctionPointer = { cb, _ -> callbackHandler(cb, ConfigSqlLogHandler) },
    bufferPointer = OpaqueBuffer::pointer,
    keyedStableRefPointer = globalMemory::keyedStableRefPointer,
    outputParamConfig = {
        useParamMemScoped(state) { statePtr ->
            native_sqlite3_config(id, statePtr)
        }
    },
    nativeConfig = { id, values ->
        val args = values.toVariadicArguments(::globalMemory)

        when (option) {
            SINGLETHREAD, MULTITHREAD, SERIALIZED -> native_sqlite3_config(id)
            is LOOKASIDE -> native_sqlite3_config(id, args[0] as Int, args[1] as Int)
            is MMAP_SIZE -> native_sqlite3_config(id, args[0] as Long, args[1] as Long)
            is MEMDB_MAXSIZE -> native_sqlite3_config(id, args[0] as Long)
            is PMASZ -> native_sqlite3_config(id, args[0] as UInt)

            is COVERING_INDEX_SCAN, is URI, is MEMSTATUS, is SMALL_MALLOC, is STMTJRNL_SPILL ->
                native_sqlite3_config(id, args[0] as Int)

            is LOG<*>, is SQLLOG<*> ->
                native_sqlite3_config(id, args[0] as COpaquePointer?, args[1] as COpaquePointer?)

            is PAGECACHE -> native_sqlite3_config(
                id,
                args[0] as COpaquePointer?,
                args[1] as Int,
                args[2] as Int
            )

            is IntOutput -> error("Unexpected configuration option : $option")
        }
    }
)

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3 =
    sqlite3(native_sqlite3_context_db_handle(context.pointer)!!)

public actual fun <AppData> sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: SqliteTextEncoding.CreateCollation,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?,
    callback: SqliteCollationCallback<in AppData>?
): SqliteResultCode = convertResultCode(
    native_sqlite3_create_collation_v2(
        db.pointer,
        name,
        encoding.value,
        db.memory.keyedStableRefPointer(collationKey(name, encoding), callback, appData, destroy),
        callbackHandler(callback, CollationHandler),
        stableRefDisposer(callback, destroy)
    )
)

public actual fun <AppData> sqlite3_create_function_v2(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: SqliteFunctionTextEncoding,
    appData: AppData,
    func: SqliteFunctionFuncCallback<in AppData>?,
    step: SqliteFunctionStepCallback<in AppData>?,
    final: SqliteFunctionFinalCallback<in AppData>?,
    destroy: SqliteDestroyCallback<in AppData>?
): SqliteResultCode = convertResultCode(
    createFunction(appData, func, step, final, destroy) { fn, fnDestroy ->
        native_sqlite3_create_function_v2(
            db.pointer,
            name,
            nArg,
            encoding.value,
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
    destroy: SqliteDestroyCallback<in AppData>?
): SqliteResultCode = convertResultCode(createVtabModule(module?.callbacks, appData) { vTabModule ->
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
    encoding: SqliteFunctionTextEncoding,
    appData: AppData,
    step: SqliteFunctionStepCallback<in AppData>?,
    final: SqliteFunctionFinalCallback<in AppData>?,
    value: SqliteFunctionValueCallback<in AppData>?,
    inverse: SqliteFunctionInverseCallback<in AppData>?,
    destroy: SqliteDestroyCallback<in AppData>?
): SqliteResultCode = convertResultCode(
    createWindowFunction(appData, step, final, value, inverse, destroy) { fn, fnDestroy ->
        native_sqlite3_create_window_function(
            db.pointer,
            name,
            nArg,
            encoding.value,
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

public actual fun sqlite3_db_cacheflush(db: sqlite3): SqliteResultCode =
    convertResultCode(native_sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: SqliteDbConfigOption,
): SqliteResultCode = commonDbConfig(
    option = option,
    bufferPointer = OpaqueBuffer::pointer,
    outParamConfig = {
        useParamMemScoped(state) { statePtr ->
            native_sqlite3_db_config(db.pointer, id, value, statePtr)
        }
    },
    nativeConfig = { id, values ->
        val args = values.toVariadicArguments(db::memory)

        @Suppress("UNCHECKED_CAST")
        when (option) {
            is IntOutput -> native_sqlite3_db_config(
                db.pointer,
                id,
                args[0] as Int,
                args[1] as CPointer<IntVar>?
            )

            is LOOKASIDE -> native_sqlite3_db_config(
                db.pointer,
                id,
                args[0] as CPointer<ByteVar>?,
                args[1] as Int,
                args[2] as Int
            )

            is MAINDBNAME -> native_sqlite3_db_config(
                db.pointer,
                id,
                args[0] as CPointer<ByteVar>?
            )

            is RESET_DATABASE -> native_sqlite3_db_config(
                db.pointer,
                id,
                args[0] as Int,
                args[1] as CPointer<IntVar>?
            )
        }
    }
)

public actual fun sqlite3_db_filename(
    db: sqlite3,
    database: String
): sqlite3_filename? = native_sqlite3_db_filename(db.pointer, database)
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
    database: String
): SqliteDbReadonlyResult =
    convertDbReadonlyResult(native_sqlite3_db_readonly(db.pointer, database))

public actual fun sqlite3_db_release_memory(db: sqlite3): SqliteResultCode =
    convertResultCode(native_sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: SqliteDbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        native_sqlite3_db_status(db.pointer, option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: SqliteDbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        native_sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): SqliteResultCode = convertResultCode(native_sqlite3_declare_vtab(db.pointer, sql))

public actual fun sqlite3_deserialize(
    db: sqlite3,
    database: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: SqliteDeserializeFlag?
): SqliteResultCode = convertResultCode(
    native_sqlite3_deserialize(
        db.pointer,
        database,
        buffer.pointer.reinterpret(),
        dbSize,
        bufferSize,
        flags?.value?.convert() ?: 0u
    )
)

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): SqliteResultCode = convertResultCode(memScoped {
    native_sqlite3_drop_modules(db.pointer, keep?.toCStringArray(this))
})

public actual fun sqlite3_errcode(db: sqlite3): SqliteResultCode =
    convertResultCode(native_sqlite3_errcode(db.pointer))

public actual fun sqlite3_errmsg(db: sqlite3): String? =
    native_sqlite3_errmsg(db.pointer)?.toKStringFromUtf8()

public actual fun sqlite3_error_offset(db: sqlite3): Int =
    native_sqlite3_error_offset(db.pointer)

public actual fun sqlite3_errstr(resultCode: SqliteResultCode): String? =
    native_sqlite3_errstr(resultCode.code)?.toKStringFromUtf8()

public actual fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: SqliteExecCallback<AppData>?
): SqliteResultCode = convertResultCode(useMemoryManager {
    memScoped {
        outErrorMessage.overriding(freeOnRead = true) {
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
    }
})

public actual fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String? {
    val pointer = native_sqlite3_expanded_sql(stmt.pointer) ?: return null
    val expandedSql = pointer.toKStringFromUtf8()
    native_sqlite3_free(pointer)
    return expandedSql
}

public actual fun sqlite3_extended_errcode(db: sqlite3): SqliteResultCode =
    convertResultCode(native_sqlite3_extended_errcode(db.pointer))

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): SqliteResultCode = convertResultCode(native_sqlite3_extended_result_codes(db.pointer, enabled))

public actual fun sqlite3_file_control(
    db: sqlite3,
    database: String?,
    opcode: SqliteFileControlOpcode
): SqliteResultCode = memScoped {
    val namePtr = database?.cstr?.ptr

    commonFileControl(
        opcode = opcode,
        control = {
            native_sqlite3_file_control(db.pointer, namePtr, opcode.code, null)
        },
        controlBuffer = { buffer ->
            native_sqlite3_file_control(db.pointer, namePtr, opcode.code, buffer?.pointer)
        },
        controlVfs = { param ->
            useParam(param) { paramPtr ->
                native_sqlite3_file_control(db.pointer, namePtr, opcode.code, paramPtr)
            }
        },
        controlInt32 = { param ->
            useParam(param) { paramPtr ->
                native_sqlite3_file_control(db.pointer, namePtr, opcode.code, paramPtr)
            }
        },
        controlInt64 = { param ->
            useParam(param) { paramPtr ->
                native_sqlite3_file_control(db.pointer, namePtr, opcode.code, paramPtr)
            }
        },
        controlString = { param, freeOnRead ->
            param.overriding(freeOnRead = freeOnRead) {
                useParam(param) { paramPtr ->
                    native_sqlite3_file_control(db.pointer, namePtr, opcode.code, paramPtr)
                }
            }
        }
    )
}

public actual fun sqlite3_finalize(stmt: sqlite3_stmt): SqliteResultCode =
    stmt.deallocate { native_sqlite3_finalize(stmt.pointer) }

public actual fun sqlite3_free(buffer: Buffer): Unit =
    native_sqlite3_free(buffer.pointer)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    native_sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    native_sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): SqliteResultCode =
    convertResultCode(native_sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    native_sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    native_sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResultCode(native_sqlite3_key(db.pointer, key.refTo(0), nKey))

public actual fun sqlite3_key_v2(
    db: sqlite3,
    database: String,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode =
    convertResultCode(native_sqlite3_key_v2(db.pointer, database, key.refTo(0), nKey))

public actual fun sqlite3_keyword_check(word: String): Int = memScoped {
    val cWord = word.cstr
    native_sqlite3_keyword_check(cWord.ptr, cWord.contentSize)
}

public actual fun sqlite3_keyword_count(): Int =
    native_sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): SqliteResultCode = convertResultCode(memScoped {
    val outSize = Int32OutputParam(0)

    outName.overriding(customSize = outSize) {
        useParams(outSize, outName) { sizePtr, namePtr ->
            native_sqlite3_keyword_name(index, namePtr, sizePtr)
        }
    }
})

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    native_sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    native_sqlite3_libversion()!!.toKStringFromUtf8()

public actual fun sqlite3_libversion_number(): Int =
    native_sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: SqliteRuntimeLimit,
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
    outDb: sqlite3.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outDb) { dbPtr ->
        native_sqlite3_open(fileName.cstr.ptr, dbPtr)
    }
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: sqlite3.OutputParam,
    flags: SqliteOpenFlag.Db,
    vfs: String?
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outDb) { dbPtr ->
        native_sqlite3_open_v2(fileName.cstr.ptr, dbPtr, flags.value, vfs?.cstr?.ptr)
    }
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): SqliteResultCode = convertResultCode(native_sqlite3_overload_function(db.pointer, name, nArg))

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: sqlite3_stmt.OutputParam,
    outOffset: Int32OutputParam?
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
        ksqlite_prepare_v2(db.pointer, sql.refTo(0), maxBytes, stmtPtr, offsetPtr)
    })

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: sqlite3_stmt.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outStmt) { stmtPtr ->
        val cSql = sql.cstr
        native_sqlite3_prepare_v2(db.pointer, cSql.ptr, cSql.contentSize, stmtPtr, null)
    }
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: SqlitePrepareFlag?,
    outStmt: sqlite3_stmt.OutputParam,
    outOffset: Int32OutputParam?
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outStmt, outOffset) { stmtPtr, offsetPtr ->
        val prepFlags = flags?.value?.convert() ?: 0u
        ksqlite_prepare_v3(db.pointer, sql.refTo(0), maxBytes, prepFlags, stmtPtr, offsetPtr)
    })

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: SqlitePrepareFlag?,
    outStmt: sqlite3_stmt.OutputParam
): SqliteResultCode = convertResultCode(memScoped {
    useParam(outStmt) { stmtPtr ->
        val csql = sql.cstr
        val prepFlags = flags?.value?.convert() ?: 0u
        native_sqlite3_prepare_v3(db.pointer, csql.ptr, csql.contentSize, prepFlags, stmtPtr, null)
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
    callback: SqlitePreupdateHookCallback<AppData>?
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
    outValue: sqlite3_value.OutputParam
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_preupdate_new(db.pointer, index, valuePtr)
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: sqlite3_value.OutputParam
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_preupdate_old(db.pointer, index, valuePtr)
})

public actual fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: SqliteProgressHandlerCallback<AppData>?
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
): SqliteResultCode = convertResultCode(native_sqlite3_rekey(db.pointer, key.refTo(0), nKey))

public actual fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode =
    convertResultCode(native_sqlite3_rekey_v2(db.pointer, dbName, key.refTo(0), nKey))

public actual fun sqlite3_release_memory(size: Int): Int =
    native_sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): SqliteResultCode =
    convertResultCode(native_sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { native_sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: SqliteDestroyCallback<ByteArray>?
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
    destroy: SqliteDestroyCallback<Buffer>?
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
    native_sqlite3_result_error(context.pointer, cMessage.ptr, cMessage.contentSize)
}

public actual fun sqlite3_result_error_code(
    context: sqlite3_context,
    result: SqliteResultCode.Failure
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
    destroy: SqliteDestroyCallback<Data>?
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
    native_sqlite3_result_text(context.pointer, cText.ptr, cText.contentSize, KSQLITE_TRANSIENT)
}

public actual fun sqlite3_result_text64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    encoding: SqliteTextEncoding.ResultText,
    destroy: SqliteDestroyCallback<Buffer>?
): Unit = native_sqlite3_result_text64(
    context.pointer,
    buffer.pointer,
    size.convert(),
    bufferDisposer(buffer, destroy),
    encoding.value.convert()
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
): SqliteResultCode = convertResultCode(native_sqlite3_result_zeroblob64(context.pointer, size))

public actual fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteRollbackHookCallback<AppData>?
) {
    native_sqlite3_rollback_hook(
        db.pointer,
        callbackHandler(callback, RollbackHookHandler),
        db.memory.keyedStableRefPointer(KEY_ROLLBACK_HOOK, callback, appData)
    )
}

public actual fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: SqliteAuthorizerCallback<AppData>?
): SqliteResultCode = convertResultCode(
    native_sqlite3_set_authorizer(
        db.pointer,
        callbackHandler(callback, AuthorizerHandler),
        db.memory.keyedStableRefPointer(KEY_SET_AUTHORIZER, callback, appData)
    )
)

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: SqliteResultCode,
    errorMessage: String?
): SqliteResultCode =
    convertResultCode(native_sqlite3_set_errmsg(db.pointer, errorCode.code, errorMessage))

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = native_sqlite3_set_last_insert_rowid(db.pointer, rowId)

public actual fun sqlite3_shutdown(): SqliteResultCode =
    convertResultCode(native_sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = native_sqlite3_snapshot_cmp(snapshot1.pointer, snapshot2.pointer)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    native_sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: sqlite3_snapshot.OutputParam
): SqliteResultCode = convertResultCode(useParamMemScoped(outSnapshot) { snapshotPtr ->
    native_sqlite3_snapshot_get(db.pointer, name, snapshotPtr)
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): SqliteResultCode =
    convertResultCode(native_sqlite3_snapshot_open(db.pointer, name, snapshot.pointer))

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): SqliteResultCode = convertResultCode(native_sqlite3_snapshot_recover(db.pointer, name))

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    native_sqlite3_soft_heap_limit64(limit)

public actual fun sqlite3_sourceid(): String =
    native_sqlite3_sourceid()!!.toKStringFromUtf8()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    native_sqlite3_sql(stmt.pointer)!!.toKStringFromUtf8()

public actual fun sqlite3_status(
    option: SqliteStatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        native_sqlite3_status(option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_status64(
    option: SqliteStatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): SqliteResultCode =
    convertResultCode(useParamsMemScoped(outCurrent, outHighwater) { curPtr, highPtr ->
        native_sqlite3_status64(option.id, curPtr, highPtr, resetFlag)
    })

public actual fun sqlite3_step(stmt: sqlite3_stmt): SqliteResultCode =
    convertResultCode(native_sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    native_sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: SqliteExplainMode
): SqliteResultCode = convertResultCode(native_sqlite3_stmt_explain(stmt.pointer, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): SqliteExplainMode =
    convertExplainMode(native_sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    native_sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: SqliteStatementStatusCounter,
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
    maxBytes: Int
): Int = native_sqlite3_strnicmp(first, second, maxBytes)

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    native_sqlite3_system_errno(db.pointer)

public actual fun sqlite3_table_column_metadata(
    db: sqlite3,
    dbName: String?,
    tableName: String,
    columnName: String,
    outDataType: Utf8OutputParam?,
    outCollationSequence: Utf8OutputParam?,
    outNotNull: Int32OutputParam?,
    outPrimaryKey: Int32OutputParam?,
    outAutoIncrement: Int32OutputParam?
): SqliteResultCode = convertResultCode(memScoped {
    val dataTypePtr = outDataType?.attach(this)
    val collationNamePtr = outCollationSequence?.attach(this)
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
        collationNamePtr?.let(outCollationSequence::detach)
        notNullPtr?.let(outNotNull::detach)
        primaryKeyPtr?.let(outPrimaryKey::detach)
        autoIncrementPtr?.let(outAutoIncrement::detach)
    }
})

public actual fun sqlite3_threadsafe(): Int = native_sqlite3_threadsafe()

public actual fun sqlite3_total_changes(db: sqlite3): Int =
    native_sqlite3_total_changes(db.pointer)

public actual fun sqlite3_total_changes64(db: sqlite3): Long =
    native_sqlite3_total_changes64(db.pointer)

public actual fun <AppData> sqlite3_trace_v2(
    db: sqlite3,
    mask: SqliteTraceEventCode?,
    appData: AppData,
    callback: SqliteTraceCallback<AppData>?
): SqliteResultCode = convertResultCode(
    native_sqlite3_trace_v2(
        db.pointer,
        mask?.value?.convert() ?: 0U,
        callbackHandler(callback, TraceHandler),
        db.memory.keyedStableRefPointer(KEY_TRACE, callback, appData)
    )
)

public actual fun sqlite3_txn_state(
    db: sqlite3,
    database: String?
): SqliteTransactionState? = convertTransactionState(native_sqlite3_txn_state(db.pointer, database))

public actual fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteUpdateHookCallback<AppData>?
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

public actual fun sqlite3_value_encoding(value: sqlite3_value): SqliteTextEncoding.ValueEncoding =
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

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): SqliteDataType =
    convertDataType(native_sqlite3_value_numeric_type(value.pointer))

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    native_sqlite3_value_subtype(value.pointer)

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    native_sqlite3_value_text(value.pointer)?.toKStringFromUtf8()

public actual fun sqlite3_value_type(value: sqlite3_value): SqliteDataType =
    convertDataType(native_sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? =
    native_sqlite3_vfs_find(name)?.let(::sqlite3_vfs)

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): SqliteResultCode = convertResultCode(
    native_sqlite3_vfs_register(vfs.pointer, makeDefault)
)

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): SqliteResultCode =
    convertResultCode(native_sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String = native_sqlite3_vtab_collation(info.pointer, index)!!
    .toKStringFromUtf8()

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: SqliteVtabConfigOption
): SqliteResultCode = commonVtabConfig(option) { id, values ->
    val args = values.toVariadicArguments(db::memory)

    when (option) {
        DIRECTONLY, INNOCUOUS, USES_ALL_SCHEMAS -> native_sqlite3_vtab_config(db.pointer, id)
        is CONSTRAINT_SUPPORT -> native_sqlite3_vtab_config(db.pointer, id, args[0] as Int)
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
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_in_first(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_in_next(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    native_sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): SqliteConflictResolutionMode =
    convertConflictResolutionMode(native_sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParamMemScoped(outValue) { valuePtr ->
    native_sqlite3_vtab_rhs_value(info.pointer, index, valuePtr)
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): SqliteResultCode = convertResultCode(native_sqlite3_wal_autocheckpoint(db.pointer, nFrame))

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    database: String?
): SqliteResultCode = convertResultCode(native_sqlite3_wal_checkpoint(db.pointer, database))

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    database: String?,
    mode: SqliteCheckpointMode,
    outNLog: Int32OutputParam?,
    outNCkpt: Int32OutputParam?
): SqliteResultCode = convertResultCode(memScoped {
    useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
        native_sqlite3_wal_checkpoint_v2(
            db.pointer,
            database?.cstr?.ptr,
            mode.id,
            nLogPtr,
            nCkptPtr
        )
    }
})

public actual fun <AppData> sqlite3_wal_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteWalHookCallback<AppData>?
) {
    native_sqlite3_wal_hook(
        db.pointer,
        callbackHandler(callback, WalHookHandler),
        db.memory.keyedStableRefPointer(KEY_WAL_HOOK, callback, appData)
    )
}