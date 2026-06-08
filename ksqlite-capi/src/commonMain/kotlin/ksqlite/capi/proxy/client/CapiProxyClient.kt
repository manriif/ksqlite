package ksqlite.capi.proxy.client

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
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.ReadableBuffer
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
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.sqlite3_vfs
import ksqlite.capi.vtab.Sqlite3VTabConfigOption
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_module

/**
 * Asynchronous proxy for the C-API.
 * 
 * The following mirror the C-API but all methods are suspend so they can be executed 
 * asynchronously.
 */
public interface CapiProxyClient {

    /**
     * See [ksqlite.capi.sqlite3_auto_extension].
     */
	public suspend fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_autovacuum_pages].
	 */
	public suspend fun <AppData> sqlite3_autovacuum_pages(
        db: sqlite3,
        appData: AppData,
        destroy: Sqlite3DestroyCallback<AppData>?,
        callback: Sqlite3AutoVacuumPagesCallback<AppData>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_backup_finish].
	 */
	public suspend fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_backup_init].
	 */
	public suspend fun sqlite3_backup_init(
        destDb: sqlite3,
        destDbName: String,
        srcDb: sqlite3,
        srcDbName: String
    ): sqlite3_backup?

    /**
	 * See [ksqlite.capi.sqlite3_backup_pagecount].
	 */
	public suspend fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int

    /**
	 * See [ksqlite.capi.sqlite3_backup_remaining].
	 */
	public suspend fun sqlite3_backup_remaining(backup: sqlite3_backup): Int

    /**
	 * See [ksqlite.capi.sqlite3_backup_step].
	 */
	public suspend fun sqlite3_backup_step(
        backup: sqlite3_backup,
        nPage: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_blob].
	 */
	public suspend fun sqlite3_bind_blob(
        stmt: sqlite3_stmt,
        index: Int,
        bytes: ByteArray,
        size: Int,
        destroy: Sqlite3DestroyCallback<ByteArray>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_blob64].
	 */
	public suspend fun sqlite3_bind_blob64(
        stmt: sqlite3_stmt,
        index: Int,
        buffer: Buffer,
        size: Long,
        destroy: Sqlite3DestroyCallback<Buffer>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_double].
	 */
	public suspend fun sqlite3_bind_double(
        stmt: sqlite3_stmt,
        index: Int,
        value: Double
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_int].
	 */
	public suspend fun sqlite3_bind_int(
        stmt: sqlite3_stmt,
        index: Int,
        value: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_int64].
	 */
	public suspend fun sqlite3_bind_int64(
        stmt: sqlite3_stmt,
        index: Int,
        value: Long
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_null].
	 */
	public suspend fun sqlite3_bind_null(
        stmt: sqlite3_stmt,
        index: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_parameter_count].
	 */
	public suspend fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int

    /**
	 * See [ksqlite.capi.sqlite3_bind_parameter_index].
	 */
	public suspend fun sqlite3_bind_parameter_index(
        stmt: sqlite3_stmt,
        name: String
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_bind_parameter_name].
	 */
	public suspend fun sqlite3_bind_parameter_name(
        stmt: sqlite3_stmt,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_bind_pointer].
	 */
	public suspend fun <Data> sqlite3_bind_pointer(
        stmt: sqlite3_stmt,
        index: Int,
        data: Data,
        type: String?,
        destroy: Sqlite3DestroyCallback<Data>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_text].
	 */
	public suspend fun sqlite3_bind_text(
        stmt: sqlite3_stmt,
        index: Int,
        value: String
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_text64].
	 */
	public suspend fun sqlite3_bind_text64(
        stmt: sqlite3_stmt,
        index: Int,
        buffer: Buffer,
        size: Long,
        encoding: Sqlite3TextEncoding.Set1,
        destroy: Sqlite3DestroyCallback<Buffer>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_value].
	 */
	public suspend fun sqlite3_bind_value(
        stmt: sqlite3_stmt,
        index: Int,
        value: sqlite3_value
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_zeroblob].
	 */
	public suspend fun sqlite3_bind_zeroblob(
        stmt: sqlite3_stmt,
        index: Int,
        size: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_bind_zeroblob64].
	 */
	public suspend fun sqlite3_bind_zeroblob64(
        stmt: sqlite3_stmt,
        index: Int,
        size: ULong
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_blob_bytes].
	 */
	public suspend fun sqlite3_blob_bytes(blob: sqlite3_blob): Int

    /**
	 * See [ksqlite.capi.sqlite3_blob_close].
	 */
	public suspend fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_blob_open].
	 */
	public suspend fun sqlite3_blob_open(
        db: sqlite3,
        databaseName: String,
        tableName: String,
        columnName: String,
        rowIndex: Long,
        flags: Sqlite3BlobOpenFlag,
        outBlob: Sqlite3BlobOutputParam
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_blob_read].
	 */
	public suspend fun sqlite3_blob_read(
        blob: sqlite3_blob,
        bytes: ByteArray,
        size: Int,
        offset: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_blob_reopen].
	 */
	public suspend fun sqlite3_blob_reopen(
        blob: sqlite3_blob,
        rowIndex: Long
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_blob_write].
	 */
	public suspend fun sqlite3_blob_write(
        blob: sqlite3_blob,
        bytes: ByteArray,
        size: Int,
        offset: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_busy_handler].
	 */
	public suspend fun <AppData> sqlite3_busy_handler(
        db: sqlite3,
        appData: AppData,
        callback: Sqlite3BusyHandlerCallback<AppData>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_busy_timeout].
	 */
	public suspend fun sqlite3_busy_timeout(
        db: sqlite3,
        millis: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_cancel_auto_extension].
	 */
	public suspend fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int

    /**
	 * See [ksqlite.capi.sqlite3_changes].
	 */
	public suspend fun sqlite3_changes(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_changes64].
	 */
	public suspend fun sqlite3_changes64(db: sqlite3): Long

    /**
	 * See [ksqlite.capi.sqlite3_clear_bindings].
	 */
	public suspend fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_close].
	 */
	public suspend fun sqlite3_close(db: sqlite3): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_close_v2].
	 */
	public suspend fun sqlite3_close_v2(db: sqlite3): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_collation_needed].
	 */
	public suspend fun <AppData> sqlite3_collation_needed(
        db: sqlite3,
        appData: AppData,
        callback: Sqlite3CollationNeededCallback<AppData>?,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_column_blob].
	 */
	public suspend fun sqlite3_column_blob(
        stmt: sqlite3_stmt,
        index: Int
    ): ByteArray?

    public fun sqlite3_column_buffer(
        stmt: sqlite3_stmt,
        index: Int
    ): ReadableBuffer?

    /**
	 * See [ksqlite.capi.sqlite3_column_bytes].
	 */
	public suspend fun sqlite3_column_bytes(
        stmt: sqlite3_stmt,
        index: Int
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_column_count].
	 */
	public suspend fun sqlite3_column_count(stmt: sqlite3_stmt): Int

    /**
	 * See [ksqlite.capi.sqlite3_column_database_name].
	 */
	public suspend fun sqlite3_column_database_name(
        stmt: sqlite3_stmt,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_column_decltype].
	 */
	public suspend fun sqlite3_column_decltype(
        stmt: sqlite3_stmt,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_column_double].
	 */
	public suspend fun sqlite3_column_double(
        stmt: sqlite3_stmt,
        index: Int
    ): Double

    /**
	 * See [ksqlite.capi.sqlite3_column_int].
	 */
	public suspend fun sqlite3_column_int(
        stmt: sqlite3_stmt,
        index: Int
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_column_int64].
	 */
	public suspend fun sqlite3_column_int64(
        stmt: sqlite3_stmt,
        index: Int
    ): Long

    /**
	 * See [ksqlite.capi.sqlite3_column_name].
	 */
	public suspend fun sqlite3_column_name(
        stmt: sqlite3_stmt,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_column_origin_name].
	 */
	public suspend fun sqlite3_column_origin_name(
        stmt: sqlite3_stmt,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_column_table_name].
	 */
	public suspend fun sqlite3_column_table_name(
        stmt: sqlite3_stmt,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_column_text].
	 */
	public suspend fun sqlite3_column_text(
        stmt: sqlite3_stmt,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_column_type].
	 */
	public suspend fun sqlite3_column_type(
        stmt: sqlite3_stmt,
        index: Int
    ): Sqlite3DataType

    /**
	 * See [ksqlite.capi.sqlite3_column_value].
	 */
	public suspend fun sqlite3_column_value(
        stmt: sqlite3_stmt,
        index: Int
    ): sqlite3_value?

    /**
	 * See [ksqlite.capi.sqlite3_commit_hook].
	 */
	public suspend fun <AppData> sqlite3_commit_hook(
        db: sqlite3,
        appData: AppData,
        callback: Sqlite3CommitHookCallback<AppData>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_compileoption_get].
	 */
	public suspend fun sqlite3_compileoption_get(index: Int): String?

    /**
	 * See [ksqlite.capi.sqlite3_compileoption_used].
	 */
	public suspend fun sqlite3_compileoption_used(optName: String): Int

    /**
	 * See [ksqlite.capi.sqlite3_complete].
	 */
	public suspend fun sqlite3_complete(sql: String): Sqlite3CompleteResult

    /**
	 * See [ksqlite.capi.sqlite3_config].
	 */
	public suspend fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_context_db_handle].
	 */
	public suspend fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3?

    public fun <AppData> sqlite3_create_collation(
        db: sqlite3,
        name: String,
        encoding: Sqlite3TextEncoding.Set0,
        appData: AppData,
        callback: Sqlite3CollationCompareCallback<AppData>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_create_collation_v2].
	 */
	public suspend fun <AppData> sqlite3_create_collation_v2(
        db: sqlite3,
        name: String,
        encoding: Sqlite3TextEncoding.Set0,
        appData: AppData,
        destroy: Sqlite3DestroyCallback<AppData>?,
        callback: Sqlite3CollationCompareCallback<AppData>?
    ): Sqlite3Result

    public fun <AppData> sqlite3_create_function(
        db: sqlite3,
        name: String,
        nArg: Int,
        encoding: Sqlite3TextEncoding,
        appData: AppData,
        func: Sqlite3FunctionFuncCallback<AppData>?,
        step: Sqlite3FunctionStepCallback<AppData>?,
        final: Sqlite3FunctionFinalCallback<AppData>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_create_function_v2].
	 */
	public suspend fun <AppData> sqlite3_create_function_v2(
        db: sqlite3,
        name: String,
        nArg: Int,
        encoding: Sqlite3TextEncoding,
        appData: AppData,
        func: Sqlite3FunctionFuncCallback<AppData>?,
        step: Sqlite3FunctionStepCallback<AppData>?,
        final: Sqlite3FunctionFinalCallback<AppData>?,
        destroy: Sqlite3DestroyCallback<AppData>?
    ): Sqlite3Result

    public fun <AppData> sqlite3_create_module(
        db: sqlite3,
        name: String,
        module: sqlite3_module<AppData>?,
        appData: AppData
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_create_module_v2].
	 */
	public suspend fun <AppData> sqlite3_create_module_v2(
        db: sqlite3,
        name: String,
        module: sqlite3_module<AppData>?,
        appData: AppData,
        destroy: Sqlite3DestroyCallback<AppData>?,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_create_window_function].
	 */
	public suspend fun <AppData> sqlite3_create_window_function(
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
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_data_count].
	 */
	public suspend fun sqlite3_data_count(stmt: sqlite3_stmt): Int

    /**
	 * See [ksqlite.capi.sqlite3_db_cacheflush].
	 */
	public suspend fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_db_config].
	 */
	public suspend fun sqlite3_db_config(
        db: sqlite3,
        option: Sqlite3DbConfigOption,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_db_filename].
	 */
	public suspend fun sqlite3_db_filename(
        db: sqlite3,
        name: String
    ): sqlite3_filename?

    /**
	 * See [ksqlite.capi.sqlite3_db_handle].
	 */
	public suspend fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3?

    /**
	 * See [ksqlite.capi.sqlite3_db_name].
	 */
	public suspend fun sqlite3_db_name(
        db: sqlite3,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_db_readonly].
	 */
	public suspend fun sqlite3_db_readonly(
        db: sqlite3,
        name: String
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_db_release_memory].
	 */
	public suspend fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_db_status].
	 */
	public suspend fun sqlite3_db_status(
        db: sqlite3,
        option: Sqlite3DbStatusOption,
        outCurrent: Int32OutputParam?,
        outHighwater: Int32OutputParam?,
        resetFlag: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_db_status64].
	 */
	public suspend fun sqlite3_db_status64(
        db: sqlite3,
        option: Sqlite3DbStatusOption,
        outCurrent: Int64OutputParam?,
        outHighwater: Int64OutputParam?,
        resetFlag: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_declare_vtab].
	 */
	public suspend fun sqlite3_declare_vtab(
        db: sqlite3,
        sql: String
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_deserialize].
	 */
	public suspend fun sqlite3_deserialize(
        db: sqlite3,
        schema: String?,
        buffer: Buffer,
        dbSize: Long,
        bufferSize: Long,
        flags: Sqlite3DeserializeFlag?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_drop_modules].
	 */
	public suspend fun sqlite3_drop_modules(
        db: sqlite3,
        keep: Array<String>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_errcode].
	 */
	public suspend fun sqlite3_errcode(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_errmsg].
	 */
	public suspend fun sqlite3_errmsg(db: sqlite3): String?

    /**
	 * See [ksqlite.capi.sqlite3_error_offset].
	 */
	public suspend fun sqlite3_error_offset(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_errstr].
	 */
	public suspend fun sqlite3_errstr(resultCode: Int): String?

    /**
	 * See [ksqlite.capi.sqlite3_exec].
	 */
	public suspend fun <AppData> sqlite3_exec(
        db: sqlite3,
        sql: String,
        outErrorMessage: Utf8OutputParam?,
        appData: AppData,
        callback: Sqlite3ExecCallback<AppData>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_expanded_sql].
	 */
	public suspend fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String?

    /**
	 * See [ksqlite.capi.sqlite3_extended_errcode].
	 */
	public suspend fun sqlite3_extended_errcode(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_extended_result_codes].
	 */
	public suspend fun sqlite3_extended_result_codes(
        db: sqlite3,
        enabled: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_file_control].
	 */
	public suspend fun sqlite3_file_control(
        db: sqlite3,
        name: String?,
        opcode: Sqlite3FileControlOpcode,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_finalize].
	 */
	public suspend fun sqlite3_finalize(stmt: sqlite3_stmt): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_free].
	 */
	public suspend fun sqlite3_free(buffer: Buffer)

    /**
	 * See [ksqlite.capi.sqlite3_get_autocommit].
	 */
	public suspend fun sqlite3_get_autocommit(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_initialize].
	 */
	public suspend fun sqlite3_initialize(): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_interrupt].
	 */
	public suspend fun sqlite3_interrupt(db: sqlite3)

    /**
	 * See [ksqlite.capi.sqlite3_is_interrupted].
	 */
	public suspend fun sqlite3_is_interrupted(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_key].
	 */
	public suspend fun sqlite3_key(
        db: sqlite3,
        key: ByteArray,
        nKey: Int,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_key_v2].
	 */
	public suspend fun sqlite3_key_v2(
        db: sqlite3,
        dbName: String,
        key: ByteArray,
        nKey: Int,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_keyword_check].
	 */
	public suspend fun sqlite3_keyword_check(word: String): Int

    /**
	 * See [ksqlite.capi.sqlite3_keyword_count].
	 */
	public suspend fun sqlite3_keyword_count(): Int

    /**
	 * See [ksqlite.capi.sqlite3_keyword_name].
	 */
	public suspend fun sqlite3_keyword_name(
        index: Int,
        outName: Utf8OutputParam,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_last_insert_rowid].
	 */
	public suspend fun sqlite3_last_insert_rowid(db: sqlite3): Long

    /**
	 * See [ksqlite.capi.sqlite3_libversion].
	 */
	public suspend fun sqlite3_libversion(): String

    /**
	 * See [ksqlite.capi.sqlite3_libversion_number].
	 */
	public suspend fun sqlite3_libversion_number(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_limit].
	 */
	public suspend fun sqlite3_limit(
        db: sqlite3,
        id: Sqlite3Limit,
        newVal: Int
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_log].
	 */
	public suspend fun sqlite3_log(
        errorCode: Int,
        message: String
    )

    /**
	 * See [ksqlite.capi.sqlite3_malloc].
	 */
	public suspend fun sqlite3_malloc(size: Int): Buffer?

    /**
	 * See [ksqlite.capi.sqlite3_malloc64].
	 */
	public suspend fun sqlite3_malloc64(size: Long): Buffer?

    /**
	 * See [ksqlite.capi.sqlite3_memory_used].
	 */
	public suspend fun sqlite3_memory_used(): Long

    /**
	 * See [ksqlite.capi.sqlite3_memory_highwater].
	 */
	public suspend fun sqlite3_memory_highwater(resetFlag: Int): Long

    /**
	 * See [ksqlite.capi.sqlite3_msize].
	 */
	public suspend fun sqlite3_msize(buffer: Buffer): ULong

    /**
	 * See [ksqlite.capi.sqlite3_next_stmt].
	 */
	public suspend fun sqlite3_next_stmt(
        db: sqlite3,
        stmt: sqlite3_stmt
    ): sqlite3_stmt?

    /**
	 * See [ksqlite.capi.sqlite3_open].
	 */
	public suspend fun sqlite3_open(
        fileName: String,
        outDb: Sqlite3OutputParam
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_open_v2].
	 */
	public suspend fun sqlite3_open_v2(
        fileName: String,
        outDb: Sqlite3OutputParam,
        flags: Sqlite3OpenFlag.Db,
        vfs: String?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_overload_function].
	 */
	public suspend fun sqlite3_overload_function(
        db: sqlite3,
        name: String,
        nArg: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_prepare_v2].
	 */
	public suspend fun sqlite3_prepare_v2(
        db: sqlite3,
        sql: ByteArray,
        maxBytes: Int,
        outStmt: Sqlite3StmtOutputParam,
        outOffset: Int32OutputParam?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_prepare_v2].
	 */
	public suspend fun sqlite3_prepare_v2(
        db: sqlite3,
        sql: String,
        outStmt: Sqlite3StmtOutputParam
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_prepare_v3].
	 */
	public suspend fun sqlite3_prepare_v3(
        db: sqlite3,
        sql: ByteArray,
        maxBytes: Int,
        flags: Sqlite3PrepareFlag?,
        outStmt: Sqlite3StmtOutputParam,
        outOffset: Int32OutputParam?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_prepare_v3].
	 */
	public suspend fun sqlite3_prepare_v3(
        db: sqlite3,
        sql: String,
        flags: Sqlite3PrepareFlag?,
        outStmt: Sqlite3StmtOutputParam
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_preupdate_blobwrite].
	 */
	public suspend fun sqlite3_preupdate_blobwrite(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_preupdate_count].
	 */
	public suspend fun sqlite3_preupdate_count(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_preupdate_depth].
	 */
	public suspend fun sqlite3_preupdate_depth(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_preupdate_hook].
	 */
	public suspend fun <AppData> sqlite3_preupdate_hook(
        db: sqlite3,
        appData: AppData,
        callback: Sqlite3PreupdateHookCallback<AppData>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_preupdate_new].
	 */
	public suspend fun sqlite3_preupdate_new(
        db: sqlite3,
        index: Int,
        outValue: Sqlite3ValueOutputParam
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_preupdate_old].
	 */
	public suspend fun sqlite3_preupdate_old(
        db: sqlite3,
        index: Int,
        outValue: Sqlite3ValueOutputParam
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_progress_handler].
	 */
	public suspend fun <AppData> sqlite3_progress_handler(
        db: sqlite3,
        nOps: Int,
        appData: AppData,
        callback: Sqlite3ProgressHandlerCallback<AppData>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_randomness].
	 */
	public suspend fun sqlite3_randomness(
        size: Int,
        buffer: Buffer
    )

    /**
	 * See [ksqlite.capi.sqlite3_realloc].
	 */
	public suspend fun sqlite3_realloc(
        buffer: Buffer,
        size: Int
    ): Buffer?

    /**
	 * See [ksqlite.capi.sqlite3_realloc64].
	 */
	public suspend fun sqlite3_realloc64(
        buffer: Buffer,
        size: Long
    ): Buffer?

    /**
	 * See [ksqlite.capi.sqlite3_rekey].
	 */
	public suspend fun sqlite3_rekey(
        db: sqlite3,
        key: ByteArray,
        nKey: Int,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_rekey_v2].
	 */
	public suspend fun sqlite3_rekey_v2(
        db: sqlite3,
        dbName: String,
        key: ByteArray,
        nKey: Int,
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_release_memory].
	 */
	public suspend fun sqlite3_release_memory(size: Int): Int

    /**
	 * See [ksqlite.capi.sqlite3_reset].
	 */
	public suspend fun sqlite3_reset(stmt: sqlite3_stmt): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_reset_auto_extension].
	 */
	public suspend fun sqlite3_reset_auto_extension()

    /**
	 * See [ksqlite.capi.sqlite3_result_blob].
	 */
	public suspend fun sqlite3_result_blob(
        context: sqlite3_context,
        bytes: ByteArray,
        size: Int,
        destroy: Sqlite3DestroyCallback<ByteArray>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_blob64].
	 */
	public suspend fun sqlite3_result_blob64(
        context: sqlite3_context,
        buffer: Buffer,
        size: Long,
        destroy: Sqlite3DestroyCallback<Buffer>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_double].
	 */
	public suspend fun sqlite3_result_double(
        context: sqlite3_context,
        value: Double
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_error].
	 */
	public suspend fun sqlite3_result_error(
        context: sqlite3_context,
        message: String
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_error_code].
	 */
	public suspend fun sqlite3_result_error_code(
        context: sqlite3_context,
        errorCode: Int
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_error_nomem].
	 */
	public suspend fun sqlite3_result_error_nomem(context: sqlite3_context)

    /**
	 * See [ksqlite.capi.sqlite3_result_error_toobig].
	 */
	public suspend fun sqlite3_result_error_toobig(context: sqlite3_context)

    /**
	 * See [ksqlite.capi.sqlite3_result_int].
	 */
	public suspend fun sqlite3_result_int(
        context: sqlite3_context,
        value: Int
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_int64].
	 */
	public suspend fun sqlite3_result_int64(
        context: sqlite3_context,
        value: Long
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_null].
	 */
	public suspend fun sqlite3_result_null(context: sqlite3_context)

    /**
	 * See [ksqlite.capi.sqlite3_result_pointer].
	 */
	public suspend fun <Data> sqlite3_result_pointer(
        context: sqlite3_context,
        data: Data,
        type: String?,
        destroy: Sqlite3DestroyCallback<Data>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_subtype].
	 */
	public suspend fun sqlite3_result_subtype(
        context: sqlite3_context,
        subtype: UInt
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_text].
	 */
	public suspend fun sqlite3_result_text(
        context: sqlite3_context,
        value: String
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_text64].
	 */
	public suspend fun sqlite3_result_text64(
        context: sqlite3_context,
        buffer: Buffer,
        size: Long,
        encoding: Sqlite3TextEncoding.Set1,
        destroy: Sqlite3DestroyCallback<Buffer>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_value].
	 */
	public suspend fun sqlite3_result_value(
        context: sqlite3_context,
        value: sqlite3_value,
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_zeroblob].
	 */
	public suspend fun sqlite3_result_zeroblob(
        context: sqlite3_context,
        size: Int
    )

    /**
	 * See [ksqlite.capi.sqlite3_result_zeroblob64].
	 */
	public suspend fun sqlite3_result_zeroblob64(
        context: sqlite3_context,
        size: ULong
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_rollback_hook].
	 */
	public suspend fun <AppData> sqlite3_rollback_hook(
        db: sqlite3,
        appData: AppData,
        callback: Sqlite3RollbackHookCallback<AppData>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_serialize].
	 */
	public suspend fun sqlite3_serialize(
        db: sqlite3,
        schema: String?,
        flags: Sqlite3SerializeFlag?
    ): Buffer?

    /**
	 * See [ksqlite.capi.sqlite3_set_authorizer].
	 */
	public suspend fun <AppData> sqlite3_set_authorizer(
        db: sqlite3,
        appData: AppData,
        callback: Sqlite3AuthorizerCallback<AppData>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_set_errmsg].
	 */
	public suspend fun sqlite3_set_errmsg(
        db: sqlite3,
        errorCode: Sqlite3Result.Failure,
        message: String?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_set_last_insert_rowid].
	 */
	public suspend fun sqlite3_set_last_insert_rowid(
        db: sqlite3,
        rowId: Long
    )

    /**
	 * See [ksqlite.capi.sqlite3_shutdown].
	 */
	public suspend fun sqlite3_shutdown(): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_sourceid].
	 */
	public suspend fun sqlite3_sourceid(): String

    /**
	 * See [ksqlite.capi.sqlite3_sql].
	 */
	public suspend fun sqlite3_sql(stmt: sqlite3_stmt): String

    /**
	 * See [ksqlite.capi.sqlite3_status].
	 */
	public suspend fun sqlite3_status(
        option: Sqlite3StatusOption,
        outCurrent: Int32OutputParam,
        outHighwater: Int32OutputParam,
        resetFlag: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_status64].
	 */
	public suspend fun sqlite3_status64(
        option: Sqlite3StatusOption,
        outCurrent: Int64OutputParam,
        outHighwater: Int64OutputParam,
        resetFlag: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_step].
	 */
	public suspend fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_stmt_busy].
	 */
	public suspend fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int

    /**
	 * See [ksqlite.capi.sqlite3_stmt_explain].
	 */
	public suspend fun sqlite3_stmt_explain(
        stmt: sqlite3_stmt,
        mode: Sqlite3ExplainMode
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_stmt_isexplain].
	 */
	public suspend fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode

    /**
	 * See [ksqlite.capi.sqlite3_stmt_readonly].
	 */
	public suspend fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int

    /**
	 * See [ksqlite.capi.sqlite3_stmt_status].
	 */
	public suspend fun sqlite3_stmt_status(
        stmt: sqlite3_stmt,
        counter: Sqlite3StatementStatusCounter,
        resetFlag: Int
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_strglob].
	 */
	public suspend fun sqlite3_strglob(
        globPattern: String,
        input: String
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_stricmp].
	 */
	public suspend fun sqlite3_stricmp(
        first: String,
        second: String
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_strlike].
	 */
	public suspend fun sqlite3_strlike(
        likePattern: String,
        input: String,
        escapeCharacter: Char
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_strnicmp].
	 */
	public suspend fun sqlite3_strnicmp(
        first: String,
        second: String,
        maxCharacters: Int
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_system_errno].
	 */
	public suspend fun sqlite3_system_errno(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_table_column_metadata].
	 */
	public suspend fun sqlite3_table_column_metadata(
        db: sqlite3,
        dbName: String?,
        tableName: String,
        columnName: String,
        outDataType: Utf8OutputParam?,
        outCollationName: Utf8OutputParam?,
        outNotNull: Int32OutputParam?,
        outPrimaryKey: Int32OutputParam?,
        outAutoIncrement: Int32OutputParam?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_total_changes].
	 */
	public suspend fun sqlite3_total_changes(db: sqlite3): Int

    /**
	 * See [ksqlite.capi.sqlite3_total_changes64].
	 */
	public suspend fun sqlite3_total_changes64(db: sqlite3): Long

    /**
	 * See [ksqlite.capi.sqlite3_trace_v2].
	 */
	public suspend fun <AppData> sqlite3_trace_v2(
        db: sqlite3,
        mask: Sqlite3TraceCode?,
        appData: AppData,
        callback: Sqlite3TraceCallback<AppData>?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_txn_state].
	 */
	public suspend fun sqlite3_txn_state(
        db: sqlite3,
        schema: String?
    ): Sqlite3TransactionState?

    /**
	 * See [ksqlite.capi.sqlite3_update_hook].
	 */
	public suspend fun <AppData> sqlite3_update_hook(
        db: sqlite3,
        appData: AppData,
        callback: Sqlite3UpdateHookCallback<AppData>?
    )

    /**
	 * See [ksqlite.capi.sqlite3_uri_boolean].
	 */
	public suspend fun sqlite3_uri_boolean(
        fileName: sqlite3_filename,
        parameter: String,
        default: Int
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_uri_int64].
	 */
	public suspend fun sqlite3_uri_int64(
        fileName: sqlite3_filename,
        parameter: String,
        default: Long
    ): Long

    /**
	 * See [ksqlite.capi.sqlite3_uri_key].
	 */
	public suspend fun sqlite3_uri_key(
        fileName: sqlite3_filename,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_uri_parameter].
	 */
	public suspend fun sqlite3_uri_parameter(
        fileName: sqlite3_filename,
        parameter: String
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_value_blob].
	 */
	public suspend fun sqlite3_value_blob(value: sqlite3_value): ByteArray?

    public fun sqlite3_value_buffer(value: sqlite3_value): ReadableBuffer?

    /**
	 * See [ksqlite.capi.sqlite3_value_bytes].
	 */
	public suspend fun sqlite3_value_bytes(value: sqlite3_value): Int

    /**
	 * See [ksqlite.capi.sqlite3_value_double].
	 */
	public suspend fun sqlite3_value_double(value: sqlite3_value): Double

    /**
	 * See [ksqlite.capi.sqlite3_value_dup].
	 */
	public suspend fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value?

    /**
	 * See [ksqlite.capi.sqlite3_value_encoding].
	 */
	public suspend fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2?

    /**
	 * See [ksqlite.capi.sqlite3_value_free].
	 */
	public suspend fun sqlite3_value_free(value: sqlite3_value)

    /**
	 * See [ksqlite.capi.sqlite3_value_frombind].
	 */
	public suspend fun sqlite3_value_frombind(value: sqlite3_value): Int

    /**
	 * See [ksqlite.capi.sqlite3_value_int].
	 */
	public suspend fun sqlite3_value_int(value: sqlite3_value): Int

    /**
	 * See [ksqlite.capi.sqlite3_value_int64].
	 */
	public suspend fun sqlite3_value_int64(value: sqlite3_value): Long

    /**
	 * See [ksqlite.capi.sqlite3_value_nochange].
	 */
	public suspend fun sqlite3_value_nochange(value: sqlite3_value): Int

    /**
	 * See [ksqlite.capi.sqlite3_value_numeric_type].
	 */
	public suspend fun sqlite3_value_numeric_type(value: sqlite3_value): Sqlite3DataType

    /**
	 * See [ksqlite.capi.sqlite3_value_subtype].
	 */
	public suspend fun sqlite3_value_subtype(value: sqlite3_value): UInt

    /**
	 * See [ksqlite.capi.sqlite3_value_text].
	 */
	public suspend fun sqlite3_value_text(value: sqlite3_value): String?

    /**
	 * See [ksqlite.capi.sqlite3_value_type].
	 */
	public suspend fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType

    /**
	 * See [ksqlite.capi.sqlite3_vfs_find].
	 */
	public suspend fun sqlite3_vfs_find(name: String?): sqlite3_vfs?

    /**
	 * See [ksqlite.capi.sqlite3_vfs_register].
	 */
	public suspend fun sqlite3_vfs_register(
        vfs: sqlite3_vfs,
        makeDefault: Int
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_vfs_unregister].
	 */
	public suspend fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_vtab_collation].
	 */
	public suspend fun sqlite3_vtab_collation(
        info: sqlite3_index_info,
        index: Int
    ): String?

    /**
	 * See [ksqlite.capi.sqlite3_vtab_config].
	 */
	public suspend fun sqlite3_vtab_config(
        db: sqlite3,
        option: Sqlite3VTabConfigOption
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_vtab_distinct].
	 */
	public suspend fun sqlite3_vtab_distinct(info: sqlite3_index_info): Int

    /**
	 * See [ksqlite.capi.sqlite3_vtab_in].
	 */
	public suspend fun sqlite3_vtab_in(
        info: sqlite3_index_info,
        index: Int,
        handle: Int
    ): Int

    /**
	 * See [ksqlite.capi.sqlite3_vtab_in_first].
	 */
	public suspend fun sqlite3_vtab_in_first(
        value: sqlite3_value,
        outValue: Sqlite3ValueOutputParam?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_vtab_in_next].
	 */
	public suspend fun sqlite3_vtab_in_next(
        value: sqlite3_value,
        outValue: Sqlite3ValueOutputParam?
    ): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_vtab_nochange].
	 */
	public suspend fun sqlite3_vtab_nochange(context: sqlite3_context): Int

    /**
	 * See [ksqlite.capi.sqlite3_vtab_on_conflict].
	 */
	public suspend fun sqlite3_vtab_on_conflict(db: sqlite3): Sqlite3Result

    /**
	 * See [ksqlite.capi.sqlite3_vtab_rhs_value].
	 */
	public suspend fun sqlite3_vtab_rhs_value(
        info: sqlite3_index_info,
        index: Int,
        outValue: Sqlite3ValueOutputParam?
    ): Sqlite3Result
}