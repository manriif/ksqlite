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
@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

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
import ksqlite.capi.handlers.destructorHandler
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.Utf8OutputParam
import ksqlite.capi.memory.notNull
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.toJniJavaObjectArray
import ksqlite.capi.memory.useParam
import ksqlite.capi.memory.useParams
import ksqlite.capi.memory.wrapOrNull
import ksqlite.capi.types.SqliteConfigOption
import ksqlite.capi.types.SqliteDbConfigOption
import ksqlite.capi.types.SqliteFileControlOpcode
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.capi.vtab.SqliteVtabConfigOption
import ksqlite.capi.vtab.createVtabModule
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_module
import ksqlite.foreign.ksqliteLoadLibrary
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
import ksqlite.foreign.ksqlite_auto_extension as jni_ksqlite_auto_extension
import ksqlite.foreign.sqlite3_autovacuum_pages as jni_sqlite3_autovacuum_pages
import ksqlite.foreign.sqlite3_backup_finish as jni_sqlite3_backup_finish
import ksqlite.foreign.sqlite3_backup_init as jni_sqlite3_backup_init
import ksqlite.foreign.sqlite3_backup_pagecount as jni_sqlite3_backup_pagecount
import ksqlite.foreign.sqlite3_backup_remaining as jni_sqlite3_backup_remaining
import ksqlite.foreign.sqlite3_backup_step as jni_sqlite3_backup_step
import ksqlite.foreign.sqlite3_bind_blob as jni_sqlite3_bind_blob
import ksqlite.foreign.sqlite3_bind_blob64 as jni_sqlite3_bind_blob64
import ksqlite.foreign.sqlite3_bind_double as jni_sqlite3_bind_double
import ksqlite.foreign.sqlite3_bind_int as jni_sqlite3_bind_int
import ksqlite.foreign.sqlite3_bind_int64 as jni_sqlite3_bind_int64
import ksqlite.foreign.sqlite3_bind_null as jni_sqlite3_bind_null
import ksqlite.foreign.sqlite3_bind_parameter_count as jni_sqlite3_bind_parameter_count
import ksqlite.foreign.sqlite3_bind_parameter_index as jni_sqlite3_bind_parameter_index
import ksqlite.foreign.sqlite3_bind_parameter_name as jni_sqlite3_bind_parameter_name
import ksqlite.foreign.sqlite3_bind_pointer as jni_sqlite3_bind_pointer
import ksqlite.foreign.sqlite3_bind_text as jni_sqlite3_bind_text
import ksqlite.foreign.sqlite3_bind_text64 as jni_sqlite3_bind_text64
import ksqlite.foreign.sqlite3_bind_value as jni_sqlite3_bind_value
import ksqlite.foreign.sqlite3_bind_zeroblob as jni_sqlite3_bind_zeroblob
import ksqlite.foreign.sqlite3_bind_zeroblob64 as jni_sqlite3_bind_zeroblob64
import ksqlite.foreign.sqlite3_blob_bytes as jni_sqlite3_blob_bytes
import ksqlite.foreign.sqlite3_blob_close as jni_sqlite3_blob_close
import ksqlite.foreign.sqlite3_blob_open as jni_sqlite3_blob_open
import ksqlite.foreign.sqlite3_blob_read as jni_sqlite3_blob_read
import ksqlite.foreign.sqlite3_blob_reopen as jni_sqlite3_blob_reopen
import ksqlite.foreign.sqlite3_blob_write as jni_sqlite3_blob_write
import ksqlite.foreign.sqlite3_busy_handler as jni_sqlite3_busy_handler
import ksqlite.foreign.sqlite3_busy_timeout as jni_sqlite3_busy_timeout
import ksqlite.foreign.sqlite3_changes as jni_sqlite3_changes
import ksqlite.foreign.sqlite3_changes64 as jni_sqlite3_changes64
import ksqlite.foreign.sqlite3_clear_bindings as jni_sqlite3_clear_bindings
import ksqlite.foreign.sqlite3_close as jni_sqlite3_close
import ksqlite.foreign.sqlite3_close_v2 as jni_sqlite3_close_v2
import ksqlite.foreign.sqlite3_collation_needed as jni_sqlite3_collation_needed
import ksqlite.foreign.sqlite3_column_blob as jni_sqlite3_column_blob
import ksqlite.foreign.sqlite3_column_bytes as jni_sqlite3_column_bytes
import ksqlite.foreign.sqlite3_column_count as jni_sqlite3_column_count
import ksqlite.foreign.sqlite3_column_database_name as jni_sqlite3_column_database_name
import ksqlite.foreign.sqlite3_column_decltype as jni_sqlite3_column_decltype
import ksqlite.foreign.sqlite3_column_double as jni_sqlite3_column_double
import ksqlite.foreign.sqlite3_column_int as jni_sqlite3_column_int
import ksqlite.foreign.sqlite3_column_int64 as jni_sqlite3_column_int64
import ksqlite.foreign.sqlite3_column_name as jni_sqlite3_column_name
import ksqlite.foreign.sqlite3_column_origin_name as jni_sqlite3_column_origin_name
import ksqlite.foreign.sqlite3_column_table_name as jni_sqlite3_column_table_name
import ksqlite.foreign.sqlite3_column_text as jni_sqlite3_column_text
import ksqlite.foreign.sqlite3_column_type as jni_sqlite3_column_type
import ksqlite.foreign.sqlite3_column_value as jni_sqlite3_column_value
import ksqlite.foreign.sqlite3_commit_hook as jni_sqlite3_commit_hook
import ksqlite.foreign.sqlite3_compileoption_get as jni_sqlite3_compileoption_get
import ksqlite.foreign.sqlite3_compileoption_used as jni_sqlite3_compileoption_used
import ksqlite.foreign.sqlite3_complete as jni_sqlite3_complete
import ksqlite.foreign.sqlite3_config as jni_sqlite3_config
import ksqlite.foreign.sqlite3_context_db_handle as jni_sqlite3_context_db_handle
import ksqlite.foreign.sqlite3_create_collation_v2 as jni_sqlite3_create_collation_v2
import ksqlite.foreign.sqlite3_create_function_v2 as jni_sqlite3_create_function_v2
import ksqlite.foreign.sqlite3_create_module_v2 as jni_sqlite3_create_module_v2
import ksqlite.foreign.sqlite3_create_window_function as jni_sqlite3_create_window_function
import ksqlite.foreign.sqlite3_data_count as jni_sqlite3_data_count
import ksqlite.foreign.sqlite3_db_cacheflush as jni_sqlite3_db_cacheflush
import ksqlite.foreign.sqlite3_db_config as jni_sqlite3_db_config
import ksqlite.foreign.sqlite3_db_filename as jni_sqlite3_db_filename
import ksqlite.foreign.sqlite3_db_handle as jni_sqlite3_db_handle
import ksqlite.foreign.sqlite3_db_name as jni_sqlite3_db_name
import ksqlite.foreign.sqlite3_db_readonly as jni_sqlite3_db_readonly
import ksqlite.foreign.sqlite3_db_release_memory as jni_sqlite3_db_release_memory
import ksqlite.foreign.sqlite3_db_status as jni_sqlite3_db_status
import ksqlite.foreign.sqlite3_db_status64 as jni_sqlite3_db_status64
import ksqlite.foreign.sqlite3_declare_vtab as jni_sqlite3_declare_vtab
import ksqlite.foreign.sqlite3_deserialize as jni_sqlite3_deserialize
import ksqlite.foreign.sqlite3_drop_modules as jni_sqlite3_drop_modules
import ksqlite.foreign.sqlite3_errcode as jni_sqlite3_errcode
import ksqlite.foreign.sqlite3_errmsg as jni_sqlite3_errmsg
import ksqlite.foreign.sqlite3_error_offset as jni_sqlite3_error_offset
import ksqlite.foreign.sqlite3_errstr as jni_sqlite3_errstr
import ksqlite.foreign.sqlite3_exec as jni_sqlite3_exec
import ksqlite.foreign.sqlite3_expanded_sql as jni_sqlite3_expanded_sql
import ksqlite.foreign.sqlite3_extended_errcode as jni_sqlite3_extended_errcode
import ksqlite.foreign.sqlite3_extended_result_codes as jni_sqlite3_extended_result_codes
import ksqlite.foreign.sqlite3_file_control as jni_sqlite3_file_control
import ksqlite.foreign.sqlite3_filename_database as jni_sqlite3_filename_database
import ksqlite.foreign.sqlite3_filename_journal as jni_sqlite3_filename_journal
import ksqlite.foreign.sqlite3_filename_wal as jni_sqlite3_filename_wal
import ksqlite.foreign.sqlite3_finalize as jni_sqlite3_finalize
import ksqlite.foreign.sqlite3_free as jni_sqlite3_free
import ksqlite.foreign.sqlite3_get_autocommit as jni_sqlite3_get_autocommit
import ksqlite.foreign.sqlite3_hard_heap_limit64 as jni_sqlite3_hard_heap_limit64
import ksqlite.foreign.sqlite3_initialize as jni_sqlite3_initialize
import ksqlite.foreign.sqlite3_interrupt as jni_sqlite3_interrupt
import ksqlite.foreign.sqlite3_is_interrupted as jni_sqlite3_is_interrupted
import ksqlite.foreign.sqlite3_key as jni_sqlite3_key
import ksqlite.foreign.sqlite3_key_v2 as jni_sqlite3_key_v2
import ksqlite.foreign.sqlite3_keyword_check as jni_sqlite3_keyword_check
import ksqlite.foreign.sqlite3_keyword_count as jni_sqlite3_keyword_count
import ksqlite.foreign.sqlite3_keyword_name as jni_sqlite3_keyword_name
import ksqlite.foreign.sqlite3_last_insert_rowid as jni_sqlite3_last_insert_rowid
import ksqlite.foreign.sqlite3_libversion as jni_sqlite3_libversion
import ksqlite.foreign.sqlite3_libversion_number as jni_sqlite3_libversion_number
import ksqlite.foreign.sqlite3_limit as jni_sqlite3_limit
import ksqlite.foreign.sqlite3_log as jni_sqlite3_log
import ksqlite.foreign.sqlite3_malloc as jni_sqlite3_malloc
import ksqlite.foreign.sqlite3_malloc64 as jni_sqlite3_malloc64
import ksqlite.foreign.sqlite3_memory_highwater as jni_sqlite3_memory_highwater
import ksqlite.foreign.sqlite3_memory_used as jni_sqlite3_memory_used
import ksqlite.foreign.sqlite3_msize as jni_sqlite3_msize
import ksqlite.foreign.sqlite3_next_stmt as jni_sqlite3_next_stmt
import ksqlite.foreign.sqlite3_open as jni_sqlite3_open
import ksqlite.foreign.sqlite3_open_v2 as jni_sqlite3_open_v2
import ksqlite.foreign.sqlite3_overload_function as jni_sqlite3_overload_function
import ksqlite.foreign.sqlite3_prepare_v2 as jni_sqlite3_prepare_v2
import ksqlite.foreign.sqlite3_prepare_v3 as jni_sqlite3_prepare_v3
import ksqlite.foreign.sqlite3_preupdate_blobwrite as jni_sqlite3_preupdate_blobwrite
import ksqlite.foreign.sqlite3_preupdate_count as jni_sqlite3_preupdate_count
import ksqlite.foreign.sqlite3_preupdate_depth as jni_sqlite3_preupdate_depth
import ksqlite.foreign.sqlite3_preupdate_hook as jni_sqlite3_preupdate_hook
import ksqlite.foreign.sqlite3_preupdate_new as jni_sqlite3_preupdate_new
import ksqlite.foreign.sqlite3_preupdate_old as jni_sqlite3_preupdate_old
import ksqlite.foreign.sqlite3_progress_handler as jni_sqlite3_progress_handler
import ksqlite.foreign.sqlite3_randomness as jni_sqlite3_randomness
import ksqlite.foreign.sqlite3_realloc as jni_sqlite3_realloc
import ksqlite.foreign.sqlite3_realloc64 as jni_sqlite3_realloc64
import ksqlite.foreign.sqlite3_rekey as jni_sqlite3_rekey
import ksqlite.foreign.sqlite3_rekey_v2 as jni_sqlite3_rekey_v2
import ksqlite.foreign.sqlite3_release_memory as jni_sqlite3_release_memory
import ksqlite.foreign.sqlite3_reset as jni_sqlite3_reset
import ksqlite.foreign.sqlite3_reset_auto_extension as jni_sqlite3_reset_auto_extension
import ksqlite.foreign.sqlite3_result_blob as jni_sqlite3_result_blob
import ksqlite.foreign.sqlite3_result_blob64 as jni_sqlite3_result_blob64
import ksqlite.foreign.sqlite3_result_double as jni_sqlite3_result_double
import ksqlite.foreign.sqlite3_result_error as jni_sqlite3_result_error
import ksqlite.foreign.sqlite3_result_error_code as jni_sqlite3_result_error_code
import ksqlite.foreign.sqlite3_result_error_nomem as jni_sqlite3_result_error_nomem
import ksqlite.foreign.sqlite3_result_error_toobig as jni_sqlite3_result_error_toobig
import ksqlite.foreign.sqlite3_result_int as jni_sqlite3_result_int
import ksqlite.foreign.sqlite3_result_int64 as jni_sqlite3_result_int64
import ksqlite.foreign.sqlite3_result_null as jni_sqlite3_result_null
import ksqlite.foreign.sqlite3_result_pointer as jni_sqlite3_result_pointer
import ksqlite.foreign.sqlite3_result_subtype as jni_sqlite3_result_subtype
import ksqlite.foreign.sqlite3_result_text as jni_sqlite3_result_text
import ksqlite.foreign.sqlite3_result_text64 as jni_sqlite3_result_text64
import ksqlite.foreign.sqlite3_result_value as jni_sqlite3_result_value
import ksqlite.foreign.sqlite3_result_zeroblob as jni_sqlite3_result_zeroblob
import ksqlite.foreign.sqlite3_result_zeroblob64 as jni_sqlite3_result_zeroblob64
import ksqlite.foreign.sqlite3_rollback_hook as jni_sqlite3_rollback_hook
import ksqlite.foreign.sqlite3_set_authorizer as jni_sqlite3_set_authorizer
import ksqlite.foreign.sqlite3_set_errmsg as jni_sqlite3_set_errmsg
import ksqlite.foreign.sqlite3_set_last_insert_rowid as jni_sqlite3_set_last_insert_rowid
import ksqlite.foreign.sqlite3_shutdown as jni_sqlite3_shutdown
import ksqlite.foreign.sqlite3_snapshot_cmp as jni_sqlite3_snapshot_cmp
import ksqlite.foreign.sqlite3_snapshot_free as jni_sqlite3_snapshot_free
import ksqlite.foreign.sqlite3_snapshot_get as jni_sqlite3_snapshot_get
import ksqlite.foreign.sqlite3_snapshot_open as jni_sqlite3_snapshot_open
import ksqlite.foreign.sqlite3_snapshot_recover as jni_sqlite3_snapshot_recover
import ksqlite.foreign.sqlite3_soft_heap_limit64 as jni_sqlite3_soft_heap_limit64
import ksqlite.foreign.sqlite3_sourceid as jni_sqlite3_sourceid
import ksqlite.foreign.sqlite3_sql as jni_sqlite3_sql
import ksqlite.foreign.sqlite3_status as jni_sqlite3_status
import ksqlite.foreign.sqlite3_status64 as jni_sqlite3_status64
import ksqlite.foreign.sqlite3_step as jni_sqlite3_step
import ksqlite.foreign.sqlite3_stmt_busy as jni_sqlite3_stmt_busy
import ksqlite.foreign.sqlite3_stmt_explain as jni_sqlite3_stmt_explain
import ksqlite.foreign.sqlite3_stmt_isexplain as jni_sqlite3_stmt_isexplain
import ksqlite.foreign.sqlite3_stmt_readonly as jni_sqlite3_stmt_readonly
import ksqlite.foreign.sqlite3_stmt_status as jni_sqlite3_stmt_status
import ksqlite.foreign.sqlite3_strglob as jni_sqlite3_strglob
import ksqlite.foreign.sqlite3_stricmp as jni_sqlite3_stricmp
import ksqlite.foreign.sqlite3_strlike as jni_sqlite3_strlike
import ksqlite.foreign.sqlite3_strnicmp as jni_sqlite3_strnicmp
import ksqlite.foreign.sqlite3_system_errno as jni_sqlite3_system_errno
import ksqlite.foreign.sqlite3_table_column_metadata as jni_sqlite3_table_column_metadata
import ksqlite.foreign.sqlite3_threadsafe as jni_sqlite3_threadsafe
import ksqlite.foreign.sqlite3_total_changes as jni_sqlite3_total_changes
import ksqlite.foreign.sqlite3_total_changes64 as jni_sqlite3_total_changes64
import ksqlite.foreign.sqlite3_trace_v2 as jni_sqlite3_trace_v2
import ksqlite.foreign.sqlite3_txn_state as jni_sqlite3_txn_state
import ksqlite.foreign.sqlite3_update_hook as jni_sqlite3_update_hook
import ksqlite.foreign.sqlite3_uri_boolean as jni_sqlite3_uri_boolean
import ksqlite.foreign.sqlite3_uri_int64 as jni_sqlite3_uri_int64
import ksqlite.foreign.sqlite3_uri_key as jni_sqlite3_uri_key
import ksqlite.foreign.sqlite3_uri_parameter as jni_sqlite3_uri_parameter
import ksqlite.foreign.sqlite3_value_blob as jni_sqlite3_value_blob
import ksqlite.foreign.sqlite3_value_bytes as jni_sqlite3_value_bytes
import ksqlite.foreign.sqlite3_value_double as jni_sqlite3_value_double
import ksqlite.foreign.sqlite3_value_dup as jni_sqlite3_value_dup
import ksqlite.foreign.sqlite3_value_encoding as jni_sqlite3_value_encoding
import ksqlite.foreign.sqlite3_value_free as jni_sqlite3_value_free
import ksqlite.foreign.sqlite3_value_frombind as jni_sqlite3_value_frombind
import ksqlite.foreign.sqlite3_value_int as jni_sqlite3_value_int
import ksqlite.foreign.sqlite3_value_int64 as jni_sqlite3_value_int64
import ksqlite.foreign.sqlite3_value_nochange as jni_sqlite3_value_nochange
import ksqlite.foreign.sqlite3_value_numeric_type as jni_sqlite3_value_numeric_type
import ksqlite.foreign.sqlite3_value_subtype as jni_sqlite3_value_subtype
import ksqlite.foreign.sqlite3_value_text as jni_sqlite3_value_text
import ksqlite.foreign.sqlite3_value_type as jni_sqlite3_value_type
import ksqlite.foreign.sqlite3_vfs_find as jni_sqlite3_vfs_find
import ksqlite.foreign.sqlite3_vfs_register as jni_sqlite3_vfs_register
import ksqlite.foreign.sqlite3_vfs_unregister as jni_sqlite3_vfs_unregister
import ksqlite.foreign.sqlite3_vtab_collation as jni_sqlite3_vtab_collation
import ksqlite.foreign.sqlite3_vtab_config as jni_sqlite3_vtab_config
import ksqlite.foreign.sqlite3_vtab_distinct as jni_sqlite3_vtab_distinct
import ksqlite.foreign.sqlite3_vtab_in as jni_sqlite3_vtab_in
import ksqlite.foreign.sqlite3_vtab_in_first as jni_sqlite3_vtab_in_first
import ksqlite.foreign.sqlite3_vtab_in_next as jni_sqlite3_vtab_in_next
import ksqlite.foreign.sqlite3_vtab_nochange as jni_sqlite3_vtab_nochange
import ksqlite.foreign.sqlite3_vtab_on_conflict as jni_sqlite3_vtab_on_conflict
import ksqlite.foreign.sqlite3_vtab_rhs_value as jni_sqlite3_vtab_rhs_value
import ksqlite.foreign.sqlite3_wal_autocheckpoint as jni_sqlite3_wal_autocheckpoint
import ksqlite.foreign.sqlite3_wal_checkpoint as jni_sqlite3_wal_checkpoint
import ksqlite.foreign.sqlite3_wal_checkpoint_v2 as jni_sqlite3_wal_checkpoint_v2
import ksqlite.foreign.sqlite3_wal_hook as jni_sqlite3_wal_hook

/**
 * Loads the native library.
 */
@Suppress("unused")
private val nativeInit = run(::ksqliteLoadLibrary)

public actual fun sqlite3_auto_extension(callback: SqliteAutoExtensionCallback): SqliteResultCode =
    autoExtensionRegister(callback) { jni_ksqlite_auto_extension(AutoExtensionHandler) }

public actual fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?,
    callback: SqliteAutovacuumPagesCallback<in AppData>?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_autovacuum_pages(
        db.pointer,
        callbackHandler(callback, appData, ::AutovacuumPagesHandler),
        destructorHandler(appData, destroy)
    )
)

public actual fun sqlite3_backup_finish(backup: sqlite3_backup): SqliteResultCode =
    convertResultCode(jni_sqlite3_backup_finish(backup.pointer))

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
): SqliteResultCode = convertResultCode(jni_sqlite3_backup_step(backup.pointer, nPage))

public actual fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: SqliteDestroyCallback<ByteArray>?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_bind_blob(stmt.pointer, index, bytes, size, destructorHandler(bytes, destroy))
)

public actual fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    destroy: SqliteDestroyCallback<Buffer>?
): SqliteResultCode = convertResultCode(
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
): SqliteResultCode = convertResultCode(jni_sqlite3_bind_double(stmt.pointer, index, value))

public actual fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_bind_int(stmt.pointer, index, value))

public actual fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): SqliteResultCode = convertResultCode(jni_sqlite3_bind_int64(stmt.pointer, index, value))

public actual fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_bind_null(stmt.pointer, index))

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
    destroy: SqliteDestroyCallback<Data>?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_bind_pointer(stmt.pointer, index, data, type, destructorHandler(data, destroy))
)

public actual fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    value: String
): SqliteResultCode = convertResultCode(jni_sqlite3_bind_text(stmt.pointer, index, value))

public actual fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: SqliteTextEncoding.BindText,
    destroy: SqliteDestroyCallback<Buffer>?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_bind_text64(
        stmt.pointer,
        index,
        buffer.pointer,
        size,
        destructorHandler(buffer, destroy),
        encoding.value
    )
)

public actual fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): SqliteResultCode = convertResultCode(jni_sqlite3_bind_value(stmt.pointer, index, value.pointer))

public actual fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_bind_zeroblob(stmt.pointer, index, size))

public actual fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): SqliteResultCode =
    convertResultCode(jni_sqlite3_bind_zeroblob64(stmt.pointer, index, size.toLong()))

public actual fun sqlite3_blob_bytes(blob: sqlite3_blob): Int =
    jni_sqlite3_blob_bytes(blob.pointer)

public actual fun sqlite3_blob_close(blob: sqlite3_blob): SqliteResultCode =
    convertResultCode(jni_sqlite3_blob_close(blob.pointer))

public actual fun sqlite3_blob_open(
    db: sqlite3,
    database: String,
    tableName: String,
    columnName: String,
    rowid: Long,
    flags: SqliteBlobOpenFlag,
    outBlob: sqlite3_blob.OutputParam
): SqliteResultCode = convertResultCode(
    useParam(outBlob) { blobPtr ->
        jni_sqlite3_blob_open(
            db.pointer,
            database,
            tableName,
            columnName,
            rowid,
            flags.value,
            blobPtr
        )
    }
)

public actual fun sqlite3_blob_read(
    blob: sqlite3_blob,
    output: ByteArray,
    size: Int,
    offset: Int
): SqliteResultCode =
    convertResultCode(jni_sqlite3_blob_read(blob.pointer, output, size, offset))

public actual fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowid: Long
): SqliteResultCode = convertResultCode(jni_sqlite3_blob_reopen(blob.pointer, rowid))

public actual fun sqlite3_blob_write(
    blob: sqlite3_blob,
    input: ByteArray,
    size: Int,
    offset: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_blob_write(blob.pointer, input, size, offset))

public actual fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: SqliteBusyHandlerCallback<AppData>?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_busy_handler(
        db.pointer,
        callbackHandler(callback, appData, ::BusyHandlerHandler)
    )
)

public actual fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_busy_timeout(db.pointer, millis))

public actual fun sqlite3_cancel_auto_extension(callback: SqliteAutoExtensionCallback): Int =
    autoExtensionUnregister(callback) { ksqlite_cancel_auto_extension(AutoExtensionHandler) }

public actual fun sqlite3_changes(db: sqlite3): Int =
    jni_sqlite3_changes(db.pointer)

public actual fun sqlite3_changes64(db: sqlite3): Long =
    jni_sqlite3_changes64(db.pointer)

public actual fun sqlite3_clear_bindings(stmt: sqlite3_stmt): SqliteResultCode =
    convertResultCode(jni_sqlite3_clear_bindings(stmt.pointer))

public actual fun sqlite3_close(db: sqlite3): SqliteResultCode =
    convertResultCode(jni_sqlite3_close(db.pointer))

public actual fun sqlite3_close_v2(db: sqlite3): SqliteResultCode =
    convertResultCode(jni_sqlite3_close_v2(db.pointer))

public actual fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: SqliteCollationNeededCallback<AppData>?,
): SqliteResultCode = convertResultCode(
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
): SqliteDataType = convertDataType(jni_sqlite3_column_type(stmt.pointer, index))

public actual fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value? = jni_sqlite3_column_value(stmt.pointer, index)
    .wrapOrNull(::sqlite3_value)

public actual fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteCommitHookCallback<AppData>?
) {
    val _ =
        jni_sqlite3_commit_hook(db.pointer, callbackHandler(callback, appData, ::CommitHookHandler))
}

public actual fun sqlite3_compileoption_get(index: Int): String? =
    jni_sqlite3_compileoption_get(index)

public actual fun sqlite3_compileoption_used(optName: String): Int =
    jni_sqlite3_compileoption_used(optName)

public actual fun sqlite3_complete(sql: String): SqliteCompleteResult =
    convertCompleteResult(jni_sqlite3_complete(sql))

public actual fun sqlite3_config(option: SqliteConfigOption): SqliteResultCode = commonConfig(
    option = option,
    logFunctionPointer = { cb, appData -> callbackHandler(cb, appData, ::ConfigLogHandler) },
    sqllogFunctionPointer = { cb, appData -> callbackHandler(cb, appData, ::ConfigSqlLogHandler) },
    keyedStableRefPointer = null,
    outputParamConfig = {
        useParam(state) { statePtr ->
            jni_sqlite3_config(id, arrayOf(statePtr))
        }
    }
) { id, values ->
    jni_sqlite3_config(id, values.toJniJavaObjectArray())
}

public actual fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3 =
    sqlite3(jni_sqlite3_context_db_handle(context.pointer))

public actual fun <AppData> sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: SqliteTextEncoding.CreateCollation,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?,
    callback: SqliteCollationCallback<in AppData>?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_create_collation_v2(
        db.pointer,
        name,
        encoding.value,
        destructorHandler(appData, destroy),
        callbackHandler(callback, appData, ::CollationHandler)
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
        jni_sqlite3_create_function_v2(
            db.pointer,
            name,
            nArg,
            encoding.value,
            fn,
            callbackHandler(fn.takeIf { func != null }, null, ::FunctionFuncHandler),
            callbackHandler(fn.takeIf { step != null }, null, ::FunctionStepHandler),
            callbackHandler(fn.takeIf { final != null }, null, ::FunctionFinalHandler),
            destructorHandler(fn, fnDestroy)
        )
    }
)

public actual fun <AppData> sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module<AppData>?,
    appData: AppData,
    destroy: SqliteDestroyCallback<in AppData>?
): SqliteResultCode = convertResultCode(
    createVtabModule(module?.callbacks, appData) { vTabModule ->
        jni_sqlite3_create_module_v2(
            db.pointer,
            name,
            module?.pointer.notNull,
            vTabModule,
            destructorHandler(appData, destroy)
        )
    }
)

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
        jni_sqlite3_create_window_function(
            db.pointer,
            name,
            nArg,
            encoding.value,
            fn,
            callbackHandler(fn.takeIf { step != null }, null, ::FunctionStepHandler),
            callbackHandler(fn.takeIf { final != null }, null, ::FunctionFinalHandler),
            callbackHandler(fn.takeIf { value != null }, null, ::FunctionValueHandler),
            callbackHandler(fn.takeIf { inverse != null }, null, ::FunctionInverseHandler),
            destructorHandler(fn, fnDestroy)
        )
    }
)

public actual fun sqlite3_data_count(stmt: sqlite3_stmt): Int =
    jni_sqlite3_data_count(stmt.pointer)

public actual fun sqlite3_db_cacheflush(db: sqlite3): SqliteResultCode =
    convertResultCode(jni_sqlite3_db_cacheflush(db.pointer))

public actual fun sqlite3_db_config(
    db: sqlite3,
    option: SqliteDbConfigOption,
): SqliteResultCode = commonDbConfig(
    option = option,
    outParamConfig = {
        useParam(state) { statePtr ->
            jni_sqlite3_db_config(db.pointer, id, arrayOf(value, statePtr))
        }
    }
) { id, values ->
    jni_sqlite3_db_config(db.pointer, id, values.toJniJavaObjectArray())
}

public actual fun sqlite3_db_filename(
    db: sqlite3,
    database: String
): sqlite3_filename? = jni_sqlite3_db_filename(db.pointer, database)
    .wrapOrNull(::sqlite3_filename)

public actual fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3? =
    jni_sqlite3_db_handle(stmt.pointer).wrapOrNull(::sqlite3)

public actual fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String? = jni_sqlite3_db_name(db.pointer, index)

public actual fun sqlite3_db_readonly(
    db: sqlite3,
    database: String
): SqliteDbReadonlyResult = convertDbReadonlyResult(jni_sqlite3_db_readonly(db.pointer, database))

public actual fun sqlite3_db_release_memory(db: sqlite3): SqliteResultCode =
    convertResultCode(jni_sqlite3_db_release_memory(db.pointer))

public actual fun sqlite3_db_status(
    db: sqlite3,
    option: SqliteDbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): SqliteResultCode = convertResultCode(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    jni_sqlite3_db_status(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_db_status64(
    db: sqlite3,
    option: SqliteDbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): SqliteResultCode = convertResultCode(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    jni_sqlite3_db_status64(db.pointer, option.id, curPtr, highPtr, resetFlag)
})

public actual fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): SqliteResultCode = convertResultCode(jni_sqlite3_declare_vtab(db.pointer, sql))

public actual fun sqlite3_deserialize(
    db: sqlite3,
    database: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: SqliteDeserializeFlag?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_deserialize(
        db.pointer,
        database,
        buffer.pointer,
        dbSize,
        bufferSize,
        flags?.value ?: 0
    )
)

public actual fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): SqliteResultCode = convertResultCode(jni_sqlite3_drop_modules(db.pointer, keep))

public actual fun sqlite3_errcode(db: sqlite3): SqliteResultCode =
    convertResultCode(jni_sqlite3_errcode(db.pointer))

public actual fun sqlite3_errmsg(db: sqlite3): String? = jni_sqlite3_errmsg(db.pointer)

public actual fun sqlite3_error_offset(db: sqlite3): Int = jni_sqlite3_error_offset(db.pointer)

public actual fun sqlite3_errstr(resultCode: SqliteResultCode): String? =
    jni_sqlite3_errstr(resultCode.code)

public actual fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: SqliteExecCallback<AppData>?
): SqliteResultCode = convertResultCode(
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

public actual fun sqlite3_extended_errcode(db: sqlite3): SqliteResultCode =
    convertResultCode(jni_sqlite3_extended_errcode(db.pointer))

public actual fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_extended_result_codes(db.pointer, enabled))

public actual fun sqlite3_file_control(
    db: sqlite3,
    database: String?,
    opcode: SqliteFileControlOpcode
): SqliteResultCode = commonFileControl(
    opcode = opcode,
    control = {
        jni_sqlite3_file_control(db.pointer, database, opcode.code, null)
    },
    controlBuffer = { buffer ->
        jni_sqlite3_file_control(db.pointer, database, opcode.code, buffer?.pointer.notNull)
    },
    controlVfs = { param ->
        useParam(param) { paramPtr ->
            jni_sqlite3_file_control(db.pointer, database, opcode.code, paramPtr)
        }
    },
    controlInt32 = { param ->
        useParam(param) { paramPtr ->
            jni_sqlite3_file_control(db.pointer, database, opcode.code, paramPtr)
        }
    },
    controlInt64 = { param ->
        useParam(param) { paramPtr ->
            jni_sqlite3_file_control(db.pointer, database, opcode.code, paramPtr)
        }
    },
    controlString = { param, _ ->
        useParam(param) { paramPtr ->
            jni_sqlite3_file_control(db.pointer, database, opcode.code, paramPtr)
        }
    }
)

public actual fun sqlite3_filename_database(fileName: sqlite3_filename): String? =
    jni_sqlite3_filename_database(fileName.pointer)

public actual fun sqlite3_filename_journal(fileName: sqlite3_filename): String? =
    jni_sqlite3_filename_journal(fileName.pointer)

public actual fun sqlite3_filename_wal(fileName: sqlite3_filename): String? =
    jni_sqlite3_filename_wal(fileName.pointer)

public actual fun sqlite3_finalize(stmt: sqlite3_stmt): SqliteResultCode =
    convertResultCode(jni_sqlite3_finalize(stmt.pointer))

public actual fun sqlite3_free(buffer: Buffer): Unit =
    jni_sqlite3_free(buffer.pointer)

public actual fun sqlite3_get_autocommit(db: sqlite3): Int =
    jni_sqlite3_get_autocommit(db.pointer)

public actual fun sqlite3_hard_heap_limit64(limit: Long): Long =
    jni_sqlite3_hard_heap_limit64(limit)

public actual fun sqlite3_initialize(): SqliteResultCode =
    convertResultCode(jni_sqlite3_initialize())

public actual fun sqlite3_interrupt(db: sqlite3): Unit =
    jni_sqlite3_interrupt(db.pointer)

public actual fun sqlite3_is_interrupted(db: sqlite3): Int =
    jni_sqlite3_is_interrupted(db.pointer)

public actual fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResultCode(jni_sqlite3_key(db.pointer, key, nKey))

public actual fun sqlite3_key_v2(
    db: sqlite3,
    database: String,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResultCode(jni_sqlite3_key_v2(db.pointer, database, key, nKey))

public actual fun sqlite3_keyword_check(word: String): Int = jni_sqlite3_keyword_check(word)

public actual fun sqlite3_keyword_count(): Int =
    jni_sqlite3_keyword_count()

public actual fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): SqliteResultCode = convertResultCode(useParam(outName) { namePtr ->
    jni_sqlite3_keyword_name(index, namePtr)
})

public actual fun sqlite3_last_insert_rowid(db: sqlite3): Long =
    jni_sqlite3_last_insert_rowid(db.pointer)

public actual fun sqlite3_libversion(): String =
    jni_sqlite3_libversion()

public actual fun sqlite3_libversion_number(): Int =
    jni_sqlite3_libversion_number()

public actual fun sqlite3_limit(
    db: sqlite3,
    id: SqliteRuntimeLimit,
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

public actual fun sqlite3_msize(buffer: Buffer): ULong =
    jni_sqlite3_msize(buffer.pointer).toULong()

public actual fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt?
): sqlite3_stmt? = jni_sqlite3_next_stmt(db.pointer, stmt?.pointer.notNull)
    .wrapOrNull(::sqlite3_stmt)

public actual fun sqlite3_open(
    fileName: String,
    outDb: sqlite3.OutputParam
): SqliteResultCode = convertResultCode(useParam(outDb) { dbPtr ->
    jni_sqlite3_open(fileName, dbPtr!!)
})

public actual fun sqlite3_open_v2(
    fileName: String,
    outDb: sqlite3.OutputParam,
    flags: SqliteOpenFlag.Db,
    vfs: String?
): SqliteResultCode = convertResultCode(useParam(outDb) { dbPtr ->
    jni_sqlite3_open_v2(fileName, dbPtr!!, flags.value, vfs)
})

public actual fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_overload_function(db.pointer, name, nArg))

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: sqlite3_stmt.OutputParam,
    outOffset: Int32OutputParam?
): SqliteResultCode = convertResultCode(useParams(outStmt, outOffset) { stmtPtr, offsetPtr ->
    ksqlite_prepare_v2(db.pointer, sql, maxBytes, stmtPtr!!, offsetPtr)
})

public actual fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: sqlite3_stmt.OutputParam
): SqliteResultCode = convertResultCode(useParam(outStmt) { stmtPtr ->
    jni_sqlite3_prepare_v2(db.pointer, sql, stmtPtr!!)
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: SqlitePrepareFlag?,
    outStmt: sqlite3_stmt.OutputParam,
    outOffset: Int32OutputParam?
): SqliteResultCode = convertResultCode(useParams(outStmt, outOffset) { stmtPtr, offsetPtr ->
    val prepFlags = flags?.value ?: 0
    ksqlite_prepare_v3(db.pointer, sql, maxBytes, prepFlags, stmtPtr!!, offsetPtr)
})

public actual fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: SqlitePrepareFlag?,
    outStmt: sqlite3_stmt.OutputParam
): SqliteResultCode = convertResultCode(useParam(outStmt) { stmtPtr ->
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
    callback: SqlitePreupdateHookCallback<AppData>?
) {
    val _ = jni_sqlite3_preupdate_hook(
        db.pointer,
        callbackHandler(callback, appData, ::PreupdateHookHandler),
    )
}

public actual fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: sqlite3_value.OutputParam
): SqliteResultCode = convertResultCode(useParam(outValue) { valuePtr ->
    jni_sqlite3_preupdate_new(db.pointer, index, valuePtr!!)
})

public actual fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: sqlite3_value.OutputParam
): SqliteResultCode = convertResultCode(useParam(outValue) { valuePtr ->
    jni_sqlite3_preupdate_old(db.pointer, index, valuePtr!!)
})

public actual fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: SqliteProgressHandlerCallback<AppData>?
) {
    jni_sqlite3_progress_handler(
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
): SqliteResultCode = convertResultCode(jni_sqlite3_rekey(db.pointer, key, nKey))

public actual fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): SqliteResultCode = convertResultCode(jni_sqlite3_rekey_v2(db.pointer, dbName, key, nKey))

public actual fun sqlite3_release_memory(size: Int): Int =
    jni_sqlite3_release_memory(size)

public actual fun sqlite3_reset(stmt: sqlite3_stmt): SqliteResultCode =
    convertResultCode(jni_sqlite3_reset(stmt.pointer))

public actual fun sqlite3_reset_auto_extension(): Unit =
    autoExtensionReset { jni_sqlite3_reset_auto_extension() }

public actual fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: SqliteDestroyCallback<ByteArray>?
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
    destroy: SqliteDestroyCallback<Buffer>?
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
    result: SqliteResultCode.Failure
): Unit = jni_sqlite3_result_error_code(context.pointer, result.code)

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
    destroy: SqliteDestroyCallback<Data>?
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
    encoding: SqliteTextEncoding.ResultText,
    destroy: SqliteDestroyCallback<Buffer>?
): Unit = jni_sqlite3_result_text64(
    context.pointer,
    buffer.pointer,
    size,
    destructorHandler(buffer, destroy),
    encoding.value
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
): SqliteResultCode =
    convertResultCode(jni_sqlite3_result_zeroblob64(context.pointer, size.toLong()))

public actual fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteRollbackHookCallback<AppData>?
) {
    val _ = jni_sqlite3_rollback_hook(
        db.pointer,
        callbackHandler(callback, appData, ::RollbackHookHandler)
    )
}

public actual fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: SqliteAuthorizerCallback<AppData>?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_set_authorizer(
        db.pointer,
        callbackHandler(callback, appData, ::AuthorizerHandler)
    )
)

public actual fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: SqliteResultCode,
    errorMessage: String?
): SqliteResultCode =
    convertResultCode(jni_sqlite3_set_errmsg(db.pointer, errorCode.code, errorMessage))

public actual fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
): Unit = jni_sqlite3_set_last_insert_rowid(db.pointer, rowId)

public actual fun sqlite3_shutdown(): SqliteResultCode =
    convertResultCode(jni_sqlite3_shutdown())

public actual fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int = jni_sqlite3_snapshot_cmp(snapshot1.pointer, snapshot2.pointer)

public actual fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Unit =
    jni_sqlite3_snapshot_free(snapshot.pointer)

public actual fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: sqlite3_snapshot.OutputParam
): SqliteResultCode = convertResultCode(useParam(outSnapshot) { snapshotPtr ->
    jni_sqlite3_snapshot_get(db.pointer, name, snapshotPtr!!)
})

public actual fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): SqliteResultCode =
    convertResultCode(jni_sqlite3_snapshot_open(db.pointer, name, snapshot.pointer))

public actual fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): SqliteResultCode = convertResultCode(jni_sqlite3_snapshot_recover(db.pointer, name))

public actual fun sqlite3_soft_heap_limit64(limit: Long): Long =
    jni_sqlite3_soft_heap_limit64(limit)

public actual fun sqlite3_sourceid(): String =
    jni_sqlite3_sourceid()

public actual fun sqlite3_sql(stmt: sqlite3_stmt): String =
    jni_sqlite3_sql(stmt.pointer)

public actual fun sqlite3_status(
    option: SqliteStatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): SqliteResultCode = convertResultCode(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    jni_sqlite3_status(option.id, curPtr!!, highPtr!!, resetFlag)
})

public actual fun sqlite3_status64(
    option: SqliteStatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): SqliteResultCode = convertResultCode(useParams(outCurrent, outHighwater) { curPtr, highPtr ->
    jni_sqlite3_status64(option.id, curPtr!!, highPtr!!, resetFlag)
})

public actual fun sqlite3_step(stmt: sqlite3_stmt): SqliteResultCode =
    convertResultCode(jni_sqlite3_step(stmt.pointer))

public actual fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int =
    jni_sqlite3_stmt_busy(stmt.pointer)

public actual fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: SqliteExplainMode
): SqliteResultCode = convertResultCode(jni_sqlite3_stmt_explain(stmt.pointer, mode.id))

public actual fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): SqliteExplainMode =
    convertExplainMode(jni_sqlite3_stmt_isexplain(stmt.pointer))

public actual fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int =
    jni_sqlite3_stmt_readonly(stmt.pointer)

public actual fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: SqliteStatementStatusCounter,
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
    maxBytes: Int
): Int = jni_sqlite3_strnicmp(first, second, maxBytes)

public actual fun sqlite3_system_errno(db: sqlite3): Int =
    jni_sqlite3_system_errno(db.pointer)

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
): SqliteResultCode {
    val dataTypePtr = outDataType?.attach()
    val collationNamePtr = outCollationSequence?.attach()
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
        collationNamePtr?.let(outCollationSequence::detach)
        notNullPtr?.let(outNotNull::detach)
        primaryKeyPtr?.let(outPrimaryKey::detach)
        autoIncrementPtr?.let(outAutoIncrement::detach)
    }

    return convertResultCode(resultCode)
}

public actual fun sqlite3_threadsafe(): Int = jni_sqlite3_threadsafe()

public actual fun sqlite3_total_changes(db: sqlite3): Int =
    jni_sqlite3_total_changes(db.pointer)

public actual fun sqlite3_total_changes64(db: sqlite3): Long =
    jni_sqlite3_total_changes64(db.pointer)

public actual fun <AppData> sqlite3_trace_v2(
    db: sqlite3,
    mask: SqliteTraceEventCode?,
    appData: AppData,
    callback: SqliteTraceCallback<AppData>?
): SqliteResultCode = convertResultCode(
    jni_sqlite3_trace_v2(
        db.pointer,
        mask?.value ?: 0,
        callbackHandler(callback, appData, ::TraceHandler)
    )
)

public actual fun sqlite3_txn_state(
    db: sqlite3,
    database: String?
): SqliteTransactionState? = convertTransactionState(jni_sqlite3_txn_state(db.pointer, database))

public actual fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteUpdateHookCallback<AppData>?
) {
    val _ =
        jni_sqlite3_update_hook(db.pointer, callbackHandler(callback, appData, ::UpdateHookHandler))
}

public actual fun sqlite3_uri_boolean(
    fileName: sqlite3_filename,
    parameter: String,
    default: Int
): Int = jni_sqlite3_uri_boolean(fileName.pointer, parameter, default)

public actual fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long = jni_sqlite3_uri_int64(fileName.pointer, parameter, default)

public actual fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String? = jni_sqlite3_uri_key(fileName.pointer, index)

public actual fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String? = jni_sqlite3_uri_parameter(fileName.pointer, parameter)

public actual fun sqlite3_value_blob(value: sqlite3_value): ByteArray? =
    jni_sqlite3_value_blob(value.pointer)

public actual fun sqlite3_value_bytes(value: sqlite3_value): Int =
    jni_sqlite3_value_bytes(value.pointer)

public actual fun sqlite3_value_double(value: sqlite3_value): Double =
    jni_sqlite3_value_double(value.pointer)

public actual fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value? =
    jni_sqlite3_value_dup(value.pointer).wrapOrNull(::sqlite3_value)

public actual fun sqlite3_value_encoding(value: sqlite3_value): SqliteTextEncoding.ValueEncoding =
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

public actual fun sqlite3_value_numeric_type(value: sqlite3_value): SqliteDataType =
    convertDataType(jni_sqlite3_value_numeric_type(value.pointer))

public actual fun sqlite3_value_subtype(value: sqlite3_value): UInt =
    jni_sqlite3_value_subtype(value.pointer).toUInt()

public actual fun sqlite3_value_text(value: sqlite3_value): String? =
    jni_sqlite3_value_text(value.pointer)

public actual fun sqlite3_value_type(value: sqlite3_value): SqliteDataType =
    convertDataType(jni_sqlite3_value_type(value.pointer))

public actual fun sqlite3_vfs_find(name: String?): sqlite3_vfs? =
    jni_sqlite3_vfs_find(name).wrapOrNull(::sqlite3_vfs)

public actual fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_vfs_register(vfs.pointer, makeDefault))

public actual fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): SqliteResultCode =
    convertResultCode(jni_sqlite3_vfs_unregister(vfs.pointer))

public actual fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String = jni_sqlite3_vtab_collation(info.pointer, index)

public actual fun sqlite3_vtab_config(
    db: sqlite3,
    option: SqliteVtabConfigOption
): SqliteResultCode = commonVtabConfig(option) { id, values ->
    jni_sqlite3_vtab_config(db.pointer, id, values.toJniJavaObjectArray())
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
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParam(outValue) { valuePtr ->
    jni_sqlite3_vtab_in_first(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParam(outValue) { valuePtr ->
    jni_sqlite3_vtab_in_next(value.pointer, valuePtr)
})

public actual fun sqlite3_vtab_nochange(context: sqlite3_context): Int =
    jni_sqlite3_vtab_nochange(context.pointer)

public actual fun sqlite3_vtab_on_conflict(db: sqlite3): SqliteConflictResolutionMode =
    convertConflictResolutionMode(jni_sqlite3_vtab_on_conflict(db.pointer))

public actual fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: sqlite3_value.OutputParam?
): SqliteResultCode = convertResultCode(useParam(outValue) { valuePtr ->
    jni_sqlite3_vtab_rhs_value(info.pointer, index, valuePtr)
})

public actual fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): SqliteResultCode = convertResultCode(jni_sqlite3_wal_autocheckpoint(db.pointer, nFrame))

public actual fun sqlite3_wal_checkpoint(
    db: sqlite3,
    database: String?
): SqliteResultCode = convertResultCode(jni_sqlite3_wal_checkpoint(db.pointer, database))

public actual fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    database: String?,
    mode: SqliteCheckpointMode,
    outNLog: Int32OutputParam?,
    outNCkpt: Int32OutputParam?
): SqliteResultCode = convertResultCode(useParams(outNLog, outNCkpt) { nLogPtr, nCkptPtr ->
    jni_sqlite3_wal_checkpoint_v2(db.pointer, database, mode.id, nLogPtr, nCkptPtr)
})

public actual fun <AppData> sqlite3_wal_hook(
    db: sqlite3,
    appData: AppData,
    callback: SqliteWalHookCallback<AppData>?
) {
    val _ = jni_sqlite3_wal_hook(db.pointer, callbackHandler(callback, appData, ::WalHookHandler))
}