@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3AutoExtensionCallback
import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3CollationCompareCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFinalCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFuncCallback
import ksqlite.capi.callbacks.Sqlite3FunctionInverseCallback
import ksqlite.capi.callbacks.Sqlite3FunctionStepCallback
import ksqlite.capi.callbacks.Sqlite3FunctionValueCallback
import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.callbacks.Sqlite3ExecCallback
import ksqlite.capi.callbacks.Sqlite3PreupdateHookCallback
import ksqlite.capi.callbacks.Sqlite3ProgressHandlerCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback
import ksqlite.capi.callbacks.Sqlite3AuthorizerCallback
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
import ksqlite.capi.types.Sqlite3OpenFlag
import ksqlite.capi.types.Sqlite3Limit
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
import ksqlite.capi.vtab.Sqlite3VTabConfigOption
import ksqlite.capi.types.Utf8OutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_filename
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.types.sqlite3_vfs
import ksqlite.capi.vtab.sqlite3_module

/**
 * Allocate or return the aggregate context for a user function.  A new  context is allocated on the
 * first call. Subsequent calls return the same context that was returned on prior calls.
 *
 * [sqlite3_aggregate_context()](https://sqlite.org/c3ref/aggregate_context.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * The [Data] instance is created on the first call and is returned on subsequent calls.
 * The [Data] instance, if created, is then made eligible for GC after Xfinalize() is getting called
 * by SQLite.
 *
 * If the [Data] instance must not be instantiated, then [factory] must be set to `null`.
 */
public inline fun <reified Data : Any> sqlite3_aggregate_context(
    context: sqlite3_context,
    noinline factory: (() -> Data)?
): Data? {
    val function = userDataInternal(context) ?: return null

    val instance = if (factory == null) {
        function.getAggregateContextOrNull(context)
    } else {
        function.getOrCreateAggregateContext(context, factory)
    }

    return castOrThrows(instance)
}

/**
 * Register a statically linked extension that is automatically loaded by every new database
 * connection.
 *
 * [sqlite3_auto_extension()](https://sqlite.org/c3ref/auto_extension.html)
 */
public expect fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result

/**
 * Register a function to be invoked prior to each autovacuum that determines the number of pages
 * to vacuum.
 *
 * [sqlite3_autovacuum_pages()](https://sqlite.org/c3ref/autovacuum_pages.html)
 */
public expect fun <AppData> sqlite3_autovacuum_pages(
    db: sqlite3,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?,
    callback: Sqlite3AutoVacuumPagesCallback<AppData>?
): Sqlite3Result

/**
 * Release all resources associated with an [sqlite3_backup]* handle.
 *
 * [sqlite3_backup_finish()](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupfinish)
 */
public expect fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result

/**
 * Create an [sqlite3_backup] process to copy the contents of [srcDbName] from connection handle
 * [srcDb] to [destDbName] in [destDb].
 * If successful, return a pointer to the new [sqlite3_backup] object.
 * If an error occurs, NULL is returned and an error code and error message stored in database
 * handle [destDb].
 *
 * [sqlite3_backup_init()](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupinit)
 */
public expect fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup?

/**
 * Return the total number of pages in the source database as of the most recent call to
 * [sqlite3_backup_step].
 *
 * [sqlite3_backup_pagecount()](https://sqlite.org/c3ref/backup_finish.html#sqlite3backuppagecount)
 */
public expect fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int

/**
 * Return the number of pages still to be backed up as of the most recent call to
 * [sqlite3_backup_step].
 *
 * [sqlite3_backup_remaining](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupremaining)
 */
public expect fun sqlite3_backup_remaining(backup: sqlite3_backup): Int

/**
 * Copy nPage pages from the source b-tree to the destination.
 *
 * [sqlite3_backup_step](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupstep)
 */
public expect fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob64()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_double()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_int()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_int64()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_null()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3Result

/**
 * Return the number of wildcards that can be potentially bound to.
 *
 * [sqlite3_bind_parameter_count()](https://sqlite.org/c3ref/bind_parameter_count.html)
 */
public expect fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt): Int

/**
 * Given a wildcard parameter name, return the index of the variable with that name.  If there is
 * no variable with the given name, return 0.
 *
 * [sqlite3_bind_parameter_index()](https://sqlite.org/c3ref/bind_parameter_index.html)
 */
public expect fun sqlite3_bind_parameter_index(
    stmt: sqlite3_stmt,
    name: String
): Int

/**
 * Return the name of a wildcard parameter.
 * Return NULL if the index is out of range or if the wildcard is unnamed.
 *
 * The result is always UTF-8.
 *
 * [sqlite3_bind_parameter_name()](https://sqlite.org/c3ref/bind_parameter_name.html)
 */
public expect fun sqlite3_bind_parameter_name(
    stmt: sqlite3_stmt,
    index: Int
): String?

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_pointer()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun <Data> sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Data,
    type: String?,
    destroy: Sqlite3DestroyCallback<Data>?
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_text()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    value: String
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_text64()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_value()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_zeroblob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_zeroblob64()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result

/**
 * Query a blob handle for the size of the data.
 *
 * [sqlite3_blob_bytes()](https://sqlite.org/c3ref/blob_bytes.html)
 */
public expect fun sqlite3_blob_bytes(blob: sqlite3_blob): Int

/**
 * Close a blob handle that was previously created using [sqlite3_blob_open].
 *
 * [sqlite3_blob_close()](https://sqlite.org/c3ref/blob_close.html)
 */
public expect fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result

/**
 * Open a blob handle.
 *
 * [sqlite3_blob_open()](https://sqlite.org/c3ref/blob_open.html)
 */
public expect fun sqlite3_blob_open(
    db: sqlite3,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Sqlite3BlobOpenFlag,
    outBlob: Sqlite3BlobOutputParam
): Sqlite3Result

/**
 * Read data from a blob handle.
 *
 * [sqlite3_blob_read()](https://sqlite.org/c3ref/blob_read.html)
 */
public expect fun sqlite3_blob_read(
    blob: sqlite3_blob,
    bytes: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result

/**
 * Move an existing blob handle to point to a different row of the same database table.
 *
 * If an error occurs, or if the specified row does not exist or does not contain a blob or text
 * value, then an error code is returned and the database handle error code and message set.
 * If this happens, then all subsequent calls to sqlite3_blob_xxx() functions (except blob_close())
 * immediately return SQLITE_ABORT.
 *
 * [sqlite3_blob_reopen()](https://sqlite.org/c3ref/blob_reopen.html)
 */
public expect fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result

/**
 * Write data to a blob handle.
 *
 * [sqlite3_blob_write()](https://sqlite.org/c3ref/blob_write.html)
 */
public expect fun sqlite3_blob_write(
    blob: sqlite3_blob,
    bytes: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result

/**
 * This routine sets the busy callback for an SQLite database to the given callback function with
 * the given argument.
 *
 * [sqlite3_busy_handler()](https://sqlite.org/c3ref/busy_handler.html)
 */
public expect fun <AppData> sqlite3_busy_handler(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3BusyHandlerCallback<AppData>?
): Sqlite3Result

/**
 * This routine installs a default busy handler that waits for the specified number of milliseconds
 * before returning 0.
 *
 * [sqlite3_busy_timeout()](https://sqlite.org/c3ref/busy_timeout.html)
 */
public expect fun sqlite3_busy_timeout(
    db: sqlite3,
    millis: Int
): Sqlite3Result

/**
 * Cancel a prior call to sqlite3_auto_extension.
 *
 * Remove [callback] from the set of routines that is invoked for each new database connection, if
 * it is currently on the list. If [callback] is not on the list, then this routine is a no-op.
 *
 * Return 1 if [callback] was found on the list and removed.
 * Return 0 if [callback]  was not on the list.
 *
 * [sqlite3_cancel_auto_extension()](https://sqlite.org/c3ref/cancel_auto_extension.html)
 */
public expect fun sqlite3_cancel_auto_extension(callback: Sqlite3AutoExtensionCallback): Int

/**
 * Return the number of changes in the most recent call to [sqlite3_exec].
 *
 * [sqlite3_changes()](https://sqlite.org/c3ref/changes.html)
 */
public expect fun sqlite3_changes(db: sqlite3): Int

/**
 * Return the number of changes in the most recent call to [sqlite3_exec].
 *
 * [sqlite3_changes64()](https://sqlite.org/c3ref/changes.html)
 */
public expect fun sqlite3_changes64(db: sqlite3): Long

/**
 * Set all the parameters in the compiled SQL statement to NULL.
 *
 * [sqlite3_clear_bindings()](https://sqlite.org/c3ref/clear_bindings.html)
 */
public expect fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Sqlite3Result

/**
 * Two variations on the public interface for closing a database.
 *
 * The sqlite3_close() version returns SQLITE_BUSY and leaves the connection open if there are
 * unfinalized prepared statements or unfinished sqlite3_backups.
 *
 * [sqlite3_close()](https://sqlite.org/c3ref/close.html)
 */
public expect fun sqlite3_close(db: sqlite3): Sqlite3Result

/**
 * Two variations on the public interface for closing a database.
 *
 * The sqlite3_close_v2() version forces the connection to become a zombie if there are unclosed
 * resources, and arranges for deallocation when the last prepare statement or sqlite3_backup
 * closes.
 *
 * [sqlite3_close_v2()](https://sqlite.org/c3ref/close.html)
 */
public expect fun sqlite3_close_v2(db: sqlite3): Sqlite3Result

/**
 * Register a collation sequence factory callback with the database handle [db].
 * Replace any previously installed collation sequence factory.
 *
 * [sqlite3_collation_needed()](https://sqlite.org/c3ref/collation_needed.html)
 */
public expect fun <AppData> sqlite3_collation_needed(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CollationNeededCallback<AppData>?,
): Sqlite3Result

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): ByteArray?

/**
 * Variant of [sqlite3_column_blob] that returns a [Buffer] instead.
 */
public fun sqlite3_column_buffer(
    stmt: sqlite3_stmt,
    index: Int
): ReadableBuffer? = columnBufferInternal(stmt, index)?.readOnly()

/**
 * The following routines are used to access elements of the current row in the result set.
 * Return the ize of a BLOB or a UTF-8 TEXT result in bytes.
 *
 * [sqlite3_column_bytes()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_bytes(
    stmt: sqlite3_stmt,
    index: Int
): Int

/**
 * Return the number of columns in the result set for the statement [stmt].
 *
 * [sqlite3_column_count()](https://sqlite.org/c3ref/column_count.html)
 */
public expect fun sqlite3_column_count(stmt: sqlite3_stmt): Int

/**
 * Return the name of the database from which a result column derives. `null` is returned if the 
 * result column is an expression or constant or anything else which is not an unambiguous reference
 * to a database column.
 *
 * [sqlite3_column_database_name()](https://sqlite.org/c3ref/column_database_name.html)
 */
public expect fun sqlite3_column_database_name(
    stmt: sqlite3_stmt,
    index: Int
): String?

/**
 * Return the column declaration type (if applicable) of the [index]-th column of the result set of
 * SQL statement [stmt].
 *
 * [sqlite3_column_decltype()](https://sqlite.org/c3ref/column_decltype.html)
 */
public expect fun sqlite3_column_decltype(
    stmt: sqlite3_stmt,
    index: Int
): String?

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_double()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_int()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_int64()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_int64(
    stmt: sqlite3_stmt,
    index: Int
): Long

/**
 * Return the name of the [index]-th column of the result set returned by SQL statement [stmt].
 *
 * [sqlite3_column_name()](https://sqlite.org/c3ref/column_name.html)
 */
public expect fun sqlite3_column_name(
    stmt: sqlite3_stmt,
    index: Int
): String?

/**
 * Return the name of the table column from which a result column derives. `null` is returned if the
 * result column is an expression or constant or anything else which is not an unambiguous reference
 * to a database column.
 *
 * [sqlite3_column_origin_name()](https://sqlite.org/c3ref/column_database_name.html)
 */
public expect fun sqlite3_column_origin_name(
    stmt: sqlite3_stmt,
    index: Int
): String?

/**
 * Return the name of the table from which a result column derives. `null` is returned if the result
 * column is an expression or constant or  anything else which is not an unambiguous reference to a
 * database column.
 *
 * [sqlite3_column_table_name()](https://sqlite.org/c3ref/column_database_name.html)
 */
public expect fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String?

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_text()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String?

/**
 * The following routines are used to access elements of the current row in the result set.
 * Return the default datatype of the result.
 *
 * [sqlite3_column_type()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3DataType

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_value()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value?

/**
 * Register a function to be invoked when a transaction commits. If the invoked function returns
 * non-zero, then the commit becomes a rollback.
 *
 * [sqlite_commit_hook()](https://sqlite.org/c3ref/commit_hook.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * The previous hook is returned by SQLite but is not returned by KSQlite for now.
 */
public expect fun <AppData> sqlite3_commit_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3CommitHookCallback<AppData>?
)

/**
 * Return the [index]-th compile-time option string. If [index] is out of range, return `null`.
 *
 * [sqlite3_compileoption_get()](https://sqlite.org/c3ref/compileoption_get.html)
 */
public expect fun sqlite3_compileoption_get(index: Int): String?

/**
 * Given the name of a compile-time option, return true if that option was used and false if not.
 *
 * The name can optionally begin with "SQLITE_" but the "SQLITE_" prefix is not required for a
 * match.
 *
 * [sqlite3_compileoption_used()](https://sqlite.org/c3ref/compileoption_get.html)
 */
public expect fun sqlite3_compileoption_used(optName: String): Int

/**
 * Return `1` if the given SQL string ends in a semicolon.
 *
 * [sqlite3_complete()](https://sqlite.org/c3ref/complete.html)
 */
public expect fun sqlite3_complete(sql: String): Sqlite3CompleteResult

/**
 * This API allows applications to modify the global configuration of the SQLite library at
 * run-time.
 *
 * This routine should only be called when there are no outstanding database connections or memory
 * allocations. This routine is not threadsafe. Failure to heed these warnings can lead to
 * unpredictable behavior.
 *
 * [sqlite3_config()](https://sqlite.org/c3ref/config.html)
 */
public expect fun sqlite3_config(option: Sqlite3ConfigOption): Sqlite3Result

/**
 * Extract the user data from a sqlite3_context structure and return a pointer to it.
 *
 * [sqlite3_context_db_handle()](https://sqlite.org/c3ref/context_db_handle.html)
 */
public expect fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3?

/**
 * Register a new collation sequence with the database handle [db].
 *
 * [sqlite3_create_collation()](https://sqlite.org/c3ref/create_collation.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * This function differs from sqlite3_create_collation_v2() in the destroy parameter only.
 * sqlite3_create_collation_v2() is used in place of sqlite3_create_collation(). That being said,
 * the semantic of both functions is the same as stated by SQLite.
 */
public fun <AppData> sqlite3_create_collation(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    appData: AppData,
    callback: Sqlite3CollationCompareCallback<AppData>?
): Sqlite3Result = sqlite3_create_collation_v2(
    db = db,
    name = name,
    encoding = encoding,
    appData = appData,
    destroy = null,
    callback = callback
)

/**
 * Register a new collation sequence with the database handle [db].
 *
 * [sqlite3_create_collation_v2()](https://sqlite.org/c3ref/create_collation.html)
 */
public expect fun <AppData> sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?,
    callback: Sqlite3CollationCompareCallback<AppData>?
): Sqlite3Result

/**
 * Create new user functions.
 *
 * [sqlite3_create_function()](https://sqlite.org/c3ref/create_function.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * This function differs from sqlite3_create_function_v2() in the destroy parameter only.
 * As KSQLite internally associates some resources with the function that need to be clenaed up
 * once the function is dropped, sqlite3_create_function_v2() is used in place of
 * sqlite3_create_function(). That being said, the semantic of both functions is the same as stated
 * by SQLite.
 */
public fun <AppData> sqlite3_create_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    appData: AppData,
    func: Sqlite3FunctionFuncCallback<AppData>?,
    step: Sqlite3FunctionStepCallback<AppData>?,
    final: Sqlite3FunctionFinalCallback<AppData>?
): Sqlite3Result = sqlite3_create_function_v2(
    db = db,
    name = name,
    nArg = nArg,
    encoding = encoding,
    appData = appData,
    func = func,
    step = step,
    final = final,
    destroy = null
)

/**
 * Create new user functions.
 *
 * [sqlite3_create_function_v2()](https://sqlite.org/c3ref/create_function.html)
 */
public expect fun <AppData> sqlite3_create_function_v2(
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

/**
 * External API function used to create a new virtual-table module.
 *
 * [sqlite3_create_module()](https://sqlite.org/c3ref/create_module.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * This function differs from sqlite3_create_module_v2() in the destroy parameter only.
 * sqlite3_create_module_v2() is used in place of sqlite3_create_module(). That being said, the
 * semantic of both functions is the same as (maybe ?) stated by SQLite.
 */
public fun <AppData> sqlite3_create_module(
    db: sqlite3,
    name: String,
    module: sqlite3_module<AppData>?,
    appData: AppData
): Sqlite3Result = sqlite3_create_module_v2(
    db = db,
    name = name,
    module = module,
    appData = appData,
    destroy = null
)

/**
 * External API function used to create a new virtual-table module.
 *
 * [sqlite3_create_module()](https://sqlite.org/c3ref/create_module.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * Drop the module previously registered with [name].
 */
public fun sqlite3_create_module(
    db: sqlite3,
    name: String,
    module: Nothing?
): Sqlite3Result = sqlite3_create_module(
    db = db,
    name = name,
    module = module,
    appData = null
)

/**
 * External API function used to create a new virtual-table module.
 *
 * [sqlite3_create_module_v2()](https://sqlite.org/c3ref/create_module.html)
 */
public expect fun <AppData> sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module<AppData>?,
    appData: AppData,
    destroy: Sqlite3DestroyCallback<AppData>?,
): Sqlite3Result

/**
 * External API function used to create a new virtual-table module.
 *
 * [sqlite3_create_module_v2()](https://sqlite.org/c3ref/create_module.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * Drop the module previously registered with [name].
 */
public fun sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: Nothing?
): Sqlite3Result = sqlite3_create_module_v2(
    db = db,
    name = name,
    module = module,
    appData = null,
    destroy = null
)

/**
 * Create new user functions.
 *
 * [sqlite3_create_window_function()](https://sqlite.org/c3ref/create_function.html)
 */
public expect fun <AppData> sqlite3_create_window_function(
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
 * Return the number of values available from the current row of the currently executing statement
 * [stmt].
 *
 * [sqlite3_data_count()](https://sqlite.org/c3ref/data_count.html)
 */
public expect fun sqlite3_data_count(stmt: sqlite3_stmt): Int

/**
 * Flush any dirty pages in the pager-cache for any attached database to disk.
 *
 * [sqlite3_db_cacheflush()](https://sqlite.org/c3ref/db_cacheflush.html)
 */
public expect fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result

/**
 * Configuration settings for an individual database connection.
 *
 * [sqlite3_db_config()](https://sqlite.org/c3ref/db_config.html)
 */
public expect fun sqlite3_db_config(
    db: sqlite3,
    option: Sqlite3DbConfigOption,
): Sqlite3Result

/**
 * Return the filename of the database associated with a database connection.
 *
 * [sqlite3_db_filename()](https://sqlite.org/c3ref/db_filename.html)
 */
public expect fun sqlite3_db_filename(
    db: sqlite3,
    name: String
): sqlite3_filename?

/**
 * Return the sqlite3* database handle to which the prepared statement given in the argument
 * belongs. This is the same database handle that was the first argument to the sqlite3_prepare()
 * that was used to create the statement in the first place.
 *
 * [sqlite3_db_handle()](https://sqlite.org/c3ref/db_handle.html)
 */
public expect fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3?

/**
 * Return the name of the [index]-th database schema. Return `null` if [index] is out of range.
 *
 * [sqlite3_db_name()](https://sqlite.org/c3ref/db_name.html)
 */
public expect fun sqlite3_db_name(
    db: sqlite3,
    index: Int
): String?

/**
 * Return 1 if database is read-only or 0 if read/write. Return -1 if no such database exists.
 *
 * [sqlite3_db_readonly()](https://sqlite.org/c3ref/db_readonly.html)
 */
public expect fun sqlite3_db_readonly(
    db: sqlite3,
    name: String
): Int

/**
 * Free up as much memory as we can from the given database connection.
 *
 * [sqlite3_db_release_memory()](https://sqlite.org/c3ref/db_release_memory.html)
 */
public expect fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result

/**
 * 32-bit variant of [sqlite3_db_status64].
 *
 * [sqlite3_db_status()](https://sqlite.org/c3ref/db_status.html)
 */
public expect fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int32OutputParam?,
    outHighwater: Int32OutputParam?,
    resetFlag: Int
): Sqlite3Result

/**
 * Query status information for a single database connection.
 *
 * [sqlite3_db_status64()](https://sqlite.org/c3ref/db_status.html)
 */
public expect fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    outCurrent: Int64OutputParam?,
    outHighwater: Int64OutputParam?,
    resetFlag: Int
): Sqlite3Result

/**
 * This function is used to set the schema of a virtual table. It is only  valid to call this
 * function from within the xCreate() or xConnect() of a virtual table module.
 *
 * [sqlite3_declare_vtab()](https://sqlite.org/c3ref/declare_vtab.html)
 */
public expect fun sqlite3_declare_vtab(
    db: sqlite3,
    sql: String
): Sqlite3Result

/**
 * Convert zSchema to a MemDB and initialize its content.
 *
 * [sqlite3_deserialize()](https://sqlite.org/c3ref/deserialize.html)
 */
public expect fun sqlite3_deserialize(
    db: sqlite3,
    schema: String?,
    buffer: Buffer,
    dbSize: Long,
    bufferSize: Long,
    flags: Sqlite3DeserializeFlag?
): Sqlite3Result

/**
 * External API to drop all virtual-table modules, except those named on the azNames list.
 *
 * [sqlite3_drop_modules()](https://sqlite.org/c3ref/drop_modules.html)
 */
public expect fun sqlite3_drop_modules(
    db: sqlite3,
    keep: Array<String>?
): Sqlite3Result

/**
 * Return the most recent error code generated by an SQLite routine. If NULL is passed to this
 * function, we assume a malloc() failed during sqlite3_open().
 *
 * [sqlite3_errcode()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_errcode(db: sqlite3): Int

/**
 * Return UTF-8 encoded English language explanation of the most recent error.
 *
 * [sqlite3_errmsg()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_errmsg(db: sqlite3): String?

/**
 * Return the byte offset of the most recent error.
 *
 * [sqlite3_error_offset()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_error_offset(db: sqlite3): Int

/**
 * Return a string that describes the kind of error specified in the argument.
 *
 * [sqlite3_errstr()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_errstr(resultCode: Int): String?

/**
 * Execute SQL code. Return one of the SQLITE_ success/failure codes. Also write an error message
 * into memory obtained from malloc() and make [outErrorMessage] point to that message.
 *
 * If the SQL is a query, then for each row in the query result the [callback] function is called.
 * If [callback]=`null` then no callback is invoked, even for queries.
 *
 * [sqlite3_exec()](https://sqlite.org/c3ref/exec.html)
 */
public expect fun <AppData> sqlite3_exec(
    db: sqlite3,
    sql: String,
    outErrorMessage: Utf8OutputParam?,
    appData: AppData,
    callback: Sqlite3ExecCallback<AppData>?
): Sqlite3Result

/**
 * Return the SQL associated with a prepared statement with bound parameters expanded. Space to hold
 * the returned string is obtained from sqlite3_malloc(). The caller is responsible for freeing the
 * returned string by passing it to sqlite3_free().
 *
 * The SQLITE_TRACE_SIZE_LIMIT puts an upper bound on the size of expanded bound parameters.
 *
 * [sqlite3_expanded_sql()](https://sqlite.org/c3ref/expanded_sql.html)
 */
public expect fun sqlite3_expanded_sql(stmt: sqlite3_stmt): String?

/**
 * Return the most recent error code generated by an SQLite routine. If NULL is passed to this
 * function, we assume a malloc() failed during sqlite3_open().
 *
 * [sqlite3_extended_errcode()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_extended_errcode(db: sqlite3): Int

/**
 * Enable or disable the extended result codes.
 *
 * [sqlite3_extended_result_codes()](https://sqlite.org/c3ref/extended_result_codes.html)
 */
public expect fun sqlite3_extended_result_codes(
    db: sqlite3,
    enabled: Int
): Sqlite3Result

/**
 *  Invoke the xFileControl method on a particular database.
 *
 * [sqlite3_file_control()](https://sqlite.org/c3ref/file_control.html)
 *
 * TODO app data
 */
public expect fun sqlite3_file_control(
    db: sqlite3,
    name: String?,
    opcode: Sqlite3FileControlOpcode,
): Sqlite3Result

/**
 * The following routine destroys a virtual machine that is created by the sqlite3_compile()
 * routine. The integer returned is an SQLITE_ success/failure code that describes the result of
 * executing the virtual machine.
 *
 * This routine sets the error code and string returned by sqlite3_errcode(), sqlite3_errmsg() and
 * sqlite3_errmsg16().
 *
 * [sqlite3_finalize()](https://sqlite.org/c3ref/finalize.html)
 */
public expect fun sqlite3_finalize(stmt: sqlite3_stmt): Sqlite3Result

/**
 * Free memory previously obtained from sqlite3Malloc().
 *
 * [sqlite3_free()](https://sqlite.org/c3ref/free.html)
 */
public expect fun sqlite3_free(buffer: Buffer?)

/**
 * Test to see whether or not the database connection is in autocommit mode. Return TRUE if it is
 * and FALSE if not. Autocommit mode is on by default. Autocommit is disabled by a BEGIN statement
 * and reenabled by the next COMMIT or ROLLBACK.
 *
 * [sqlite3_get_autocommit()](https://sqlite.org/c3ref/get_autocommit.html)
 */
public expect fun sqlite3_get_autocommit(db: sqlite3): Int

/**
 * Return the auxiliary data pointer, if any, for the [index]-th argument to the user-function
 * defined by [context].
 *
 * The left-most argument is 0.
 *
 * Undocumented behavior:  If [index] is negative then access a cache of auxiliary data pointers
 * that is available to all functions within a single prepared statement. The [index] values must
 * match.
 *
 * [sqlite3_get_auxdata()](https://sqlite.org/c3ref/get_auxdata.html)
 */
public inline fun <reified Data : Any> sqlite3_get_auxdata(
    context: sqlite3_context,
    index: Int
): Data? = castOrThrows(userDataInternal(context)?.getAuxiliaryDataOrNull(context, index))

/**
 * Initialize SQLite.
 *
 * This routine must be called to initialize the memory allocation, VFS, and mutex subsystems prior
 * to doing any serious work with SQLite. But as long as you do not compile with
 * SQLITE_OMIT_AUTOINIT this routine will be called automatically by key routines such as
 * sqlite3_open().
 *
 * This routine is a no-op except on its very first call for the process, or for the first call
 * after a call to sqlite3_shutdown.
 *
 * The first thread to call this routine runs the initialization to completion. If subsequent
 * threads call this routine before the first thread has finished the initialization process, then
 * the subsequent threads must block until the first thread finishes with the initialization.
 *
 * The first thread might call this routine recursively. Recursive calls to this routine should not
 * block, of course. Otherwise the initialization process would never complete.
 *
 * Let X be the first thread to enter this routine. Let Y be some other thread. Then while the
 * initial invocation of this routine by X is incomplete, it is required that:
 *
 * Calls to this routine from Y must block until the outer-most call by X completes.
 *
 * Recursive calls to this routine from thread X return immediately without blocking.
 *
 * [sqlite3_initialize()](https://sqlite.org/c3ref/initialize.html)
 */
public expect fun sqlite3_initialize(): Sqlite3Result

/**
 * Cause any pending operation to stop at its earliest opportunity.
 *
 * [sqlite3_interrupt()](https://sqlite.org/c3ref/interrupt.html)
 */
public expect fun sqlite3_interrupt(db: sqlite3)

/**
 * Return true or false depending on whether or not an interrupt is pending on connection db.
 *
 * [sqlite3_is_interrupted()](https://sqlite.org/c3ref/interrupt.html)
 */
public expect fun sqlite3_is_interrupted(db: sqlite3): Int

/**
 * sqlite3_key() set the database key to use when accessing an encrypted database, and should
 * usually be called immediately after sqlite3_open().
 *
 * sqlite3_key() is used to set the key for the main database.
 *
 * If sqlite3_key() is called on an empty database, then the key will be initially set. The return
 * value is SQLITE_OK on success, or a non-zero SQLite3 error code on failure.
 */
public expect fun sqlite3_key(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result

/**
 * sqlite3_key_v2() set the database key to use when accessing an encrypted database, and should
 * usually be called immediately after sqlite3_open().
 *
 * sqlite3_key_v2() sets the key for the database with the schema name specified by zDbName. The
 * schema name is main for the main database, temp for the temporary database, or the schema name
 * specified in an ATTACH statement for an attached database.
 *
 * If sqlite3_key_v2() is called on an empty database, then the key will be  initially set. The
 * return value is SQLITE_OK on success, or a non-zero SQLite3 error code on failure.
 */
public expect fun sqlite3_key_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result

/**
 * The sqlite3_keyword_check() interface checks to see whether or not [word] is a keyword, returning
 * `true` if it is and `false` if not.
 *
 * [sqlite3_keyword_check()](https://sqlite.org/c3ref/keyword_check.html)
 */
public expect fun sqlite3_keyword_check(word: String): Int

/**
 * The sqlite3_keyword_count() interface returns the number of distinct keywords understood by
 * SQLite.
 *
 * [sqlite3_keyword_count()](https://sqlite.org/c3ref/keyword_check.html)
 */
public expect fun sqlite3_keyword_count(): Int

/**
 * The sqlite3_keyword_name() interface finds the 0-based [index]-th keyword and makes [outName]
 * point to that keyword expressed as UTF8. The
 * string that [outName] points to is not zero-terminated. The sqlite3_keyword_name routine returns
 * SQLITE_OK if [index] is within bounds and SQLITE_ERROR if not.
 *
 * [sqlite3_keyword_name()](https://sqlite.org/c3ref/keyword_check.html)
 */
public expect fun sqlite3_keyword_name(
    index: Int,
    outName: Utf8OutputParam,
): Sqlite3Result

/**
 * Return the ROWID of the most recent insert.
 *
 * [sqlite3_last_insert_rowid()](https://sqlite.org/c3ref/last_insert_rowid.html)
 */
public expect fun sqlite3_last_insert_rowid(db: sqlite3): Long

/**
 * Return the SQLite version in the format "X.Y.Z" where X is the major version number (always 3 for
 * SQLite3) and Y is the minor version number and Z is the release number.
 *
 * [sqlite3_libversion()](https://sqlite.org/c3ref/libversion.html)
 */
public expect fun sqlite3_libversion(): String

/**
 * Return an integer with the value (X*1000000 + Y*1000 + Z) where X, Y, and Z are the same numbers
 * used in [sqlite3_libversion].
 *
 * [sqlite3_libversion_number()](https://sqlite.org/c3ref/libversion.html)
 */
public expect fun sqlite3_libversion_number(db: sqlite3): Int

/**
 * Change the value of a limit. Report the old value. If an invalid limit index is supplied,
 * report -1.
 * Make no changes but still report the old value if the new limit is negative.
 *
 * A new lower limit does not shrink existing constructs.
 * It merely prevents new constructs that exceed the limit from forming.
 *
 * [sqlite3_limit()](https://sqlite.org/c3ref/limit.html)
 */
public expect fun sqlite3_limit(
    db: sqlite3,
    id: Sqlite3Limit,
    newVal: Int
): Int

/**
 * Write a [message] to the log if logging is enabled.
 *
 * [sqlite3_log()](https://sqlite.org/c3ref/log.html)
 */
public expect fun sqlite3_log(
    errorCode: Int,
    message: String
)

/**
 * This version of the memory allocation is for use by the application.
 * First make sure the memory subsystem is initialized, then do the allocation.
 *
 * [sqlite3_malloc()](https://sqlite.org/c3ref/free.html)
 */
public expect fun sqlite3_malloc(size: Int): Buffer?

/**
 * This version of the memory allocation is for use by the application.
 * First make sure the memory subsystem is initialized, then do the allocation.
 *
 * [sqlite3_malloc64()](https://sqlite.org/c3ref/free.html)
 */
public expect fun sqlite3_malloc64(size: Long): Buffer?

/**
 * Return the amount of memory currently checked out.
 *
 * [sqlite3_memory_used()](https://sqlite.org/c3ref/memory_highwater.html)
 */
public expect fun sqlite3_memory_used(): Long

/**
 * Return the maximum amount of memory that has ever been checked out since either the beginning of
 * this process or since the most recent reset.
 *
 * [sqlite3_memory_highwater()](https://sqlite.org/c3ref/memory_highwater.html)
 */
public expect fun sqlite3_memory_highwater(resetFlag: Int): Long

/**
 * Returns the size of [buffer] memory allocation in bytes. The value returned by [sqlite3_msize]
 * might be larger than the number of bytes requested when [buffer] was allocated. If [buffer] is a
 * NULL pointer then [sqlite3_msize] returns zero. If [buffer] points to something that is not the
 * beginning of memory allocation, or if it points to a formerly valid memory allocation that has
 * now been freed, then the behavior of [sqlite3_msize] is undefined and possibly harmful.
 *
 * [sqlite3_msize()](https://sqlite.org/c3ref/free.html)
 */
public expect fun sqlite3_msize(buffer: Buffer?): ULong

/**
 * Return a pointer to the next prepared statement after pStmt associated with database connection
 * [db]. If [stmt] is NULL, return the first prepared statement for the database connection.
 * Return NULL if there are no more.
 *
 * [sqlite3_next_stmt()](https://sqlite.org/c3ref/next_stmt.html)
 */
public expect fun sqlite3_next_stmt(
    db: sqlite3,
    stmt: sqlite3_stmt?
): sqlite3_stmt?

/**
 * Open a new database handle.
 *
 * [sqlite3_open()](https://sqlite.org/c3ref/open.html)
 */
public expect fun sqlite3_open(
    fileName: String,
    outDb: Sqlite3OutputParam
): Sqlite3Result

/**
 * Open a new database handle.
 *
 * [sqlite3_open_v2()](https://sqlite.org/c3ref/open.html)
 */
public expect fun sqlite3_open_v2(
    fileName: String,
    outDb: Sqlite3OutputParam,
    flags: Sqlite3OpenFlag.Db,
    vfs: String?
): Sqlite3Result

/**
 * Declare that a function has been overloaded by a virtual table.
 *
 * If the function already exists as a regular global function, then this routine is a no-op.
 * If the function does not exist, then create a new one that always throws a run-time error.
 *
 * When virtual tables intend to provide an overloaded function, they should call this routine to
 * make sure the global function exists.
 * A global function must exist in order for name resolution to work properly.
 *
 * [sqlite3_overload_function()](https://sqlite.org/c3ref/overload_function.html)
 */
public expect fun sqlite3_overload_function(
    db: sqlite3,
    name: String,
    nArg: Int
): Sqlite3Result

/**
 * To execute an SQL statement, it must first be compiled into a byte-code program using one of
 * [sqlite3_prepare_v2] or [sqlite3_prepare_v3]. Or, in other words, these routines are constructors
 * for the prepared statement object.
 *
 * [sqlite3_prepare_v2()](https://sqlite.org/c3ref/prepare.html)
 */
public expect fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result

/**
 * To execute an SQL statement, it must first be compiled into a byte-code program using one of
 * [sqlite3_prepare_v2] or [sqlite3_prepare_v3]. Or, in other words, these routines are constructors
 * for the prepared statement object.
 *
 * [sqlite3_prepare_v2()](https://sqlite.org/c3ref/prepare.html)
 */
public expect fun sqlite3_prepare_v2(
    db: sqlite3,
    sql: String,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result

/**
 * To execute an SQL statement, it must first be compiled into a byte-code program using one of
 * [sqlite3_prepare_v2] or [sqlite3_prepare_v3]. Or, in other words, these routines are constructors
 * for the prepared statement object.
 *
 * [sqlite3_prepare_v3()](https://sqlite.org/c3ref/prepare.html)
 */
public expect fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: ByteArray,
    maxBytes: Int,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam,
    outOffset: Int32OutputParam?
): Sqlite3Result

/**
 * To execute an SQL statement, it must first be compiled into a byte-code program using one of
 * [sqlite3_prepare_v2] or [sqlite3_prepare_v3]. Or, in other words, these routines are constructors
 * for the prepared statement object.
 *
 * [sqlite3_prepare_v3()](https://sqlite.org/c3ref/prepare.html)
 */
public expect fun sqlite3_prepare_v3(
    db: sqlite3,
    sql: String,
    flags: Sqlite3PrepareFlag?,
    outStmt: Sqlite3StmtOutputParam
): Sqlite3Result

/**
 * This function is designed to be called from within a pre-update callback only.
 *
 * [sqlite3_preupdate_blobwrite()](https://sqlite.org/c3ref/preupdate_blobwrite.html)
 */
public expect fun sqlite3_preupdate_blobwrite(db: sqlite3): Int

/**
 * This function is called from within a pre-update callback to retrieve the number of columns in
 * the row being updated, deleted or inserted.
 *
 * [sqlite3_preupdate_count()](https://sqlite.org/c3ref/preupdate_blobwrite.html)
 */
public expect fun sqlite3_preupdate_count(db: sqlite3): Int

/**
 * This function is designed to be called from within a pre-update callback only. It returns zero if
 * the change that caused the callback was made immediately by a user SQL statement. Or, if the
 * change was made by a trigger program, it returns the number of trigger programs currently on the
 * stack (1 for a top-level trigger, 2 for a trigger fired by a top-level trigger etc.).
 *
 * For the purposes of the previous paragraph, a foreign key CASCADE, SET NULL or SET DEFAULT action
 * is considered a trigger.
 *
 * [sqlite3_preupdate_depth()](https://sqlite.org/c3ref/preupdate_blobwrite.html)
 */
public expect fun sqlite3_preupdate_depth(db: sqlite3): Int

/**
 * Register a callback to be invoked each time a row is updated, inserted or deleted using this
 * database connection.
 *
 * [sqlite3_preupdate_hook()](https://sqlite.org/c3ref/preupdate_blobwrite.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * The previous hook is returned by SQLite but is not returned by KSQlite for now.
 */
public expect fun <AppData> sqlite3_preupdate_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3PreupdateHookCallback<AppData>?
)

/**
 * This function is called from within a pre-update callback to retrieve a field of the row
 * currently being updated or inserted.
 *
 * [sqlite3_preupdate_new()](https://sqlite.org/c3ref/preupdate_blobwrite.html)
 */
public expect fun sqlite3_preupdate_new(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result

/**
 * This function is called from within a pre-update callback to retrieve a field of the row
 * currently being updated or deleted.
 *
 * [sqlite3_preupdate_old()](https://sqlite.org/c3ref/preupdate_blobwrite.html)
 */
public expect fun sqlite3_preupdate_old(
    db: sqlite3,
    index: Int,
    outValue: Sqlite3ValueOutputParam
): Sqlite3Result

/**
 * This routine sets the progress callback for an Sqlite database to the given callback function
 * with the given argument. The progress callback will be invoked every nOps opcodes.
 *
 * [sqlite3_progress_handler()](https://sqlite.org/c3ref/progress_handler.html)
 */
public expect fun <AppData> sqlite3_progress_handler(
    db: sqlite3,
    nOps: Int,
    appData: AppData,
    callback: Sqlite3ProgressHandlerCallback<AppData>?
)

/**
 * Return [size] random bytes.
 *
 * [sqlite3_randomness()](https://sqlite.org/c3ref/randomness.html)
 */
public expect fun sqlite3_randomness(
    size: Int,
    buffer: Buffer
)

/**
 * The public interface to sqlite3Realloc. Make sure that the memory subsystem is initialized prior
 * to invoking sqliteRealloc.
 *
 * [sqlite3_realloc()](https://sqlite.org/c3ref/free.html)
 */
public expect fun sqlite3_realloc(
    buffer: Buffer,
    size: Int
): Buffer?

/**
 * The public interface to sqlite3Realloc. Make sure that the memory subsystem is initialized prior
 * to invoking sqliteRealloc.
 *
 * [sqlite3_realloc64()](https://sqlite.org/c3ref/free.html)
 */
public expect fun sqlite3_realloc64(
    buffer: Buffer,
    size: Long
): Buffer?

/**
 * sqlite3_rekey() change the database encryption key.
 *
 * sqlite3_rekey() is used to change the key for the main database.
 *
 * Changing the key includes encrypting the database for the first time, decrypting the database
 * (if pKey == NULL or nKey == 0), as well as re-encrypting it with a new key. The return value is
 * SQLITE_OK on success, or a non-zero SQLite3 error code on failure.
 */
public expect fun sqlite3_rekey(
    db: sqlite3,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result

/**
 * sqlite3_rekey_v2() change the database encryption key.
 *
 * sqlite3_rekey_v2() changes the key for the database with the schema name specified by zDbName.
 * The schema name is main for the main database, temp for the temporary database, or the schema
 * name specified in an ATTACH statement for an attached database.
 *
 * Changing the key includes encrypting the database for the first time, decrypting the database
 * (if pKey == NULL or nKey == 0), as well as re-encrypting it with a new key. The return value is
 * SQLITE_OK on success, or a non-zero SQLite3 error code on failure.
 */
public expect fun sqlite3_rekey_v2(
    db: sqlite3,
    dbName: String,
    key: ByteArray,
    nKey: Int,
): Sqlite3Result

/**
 * Attempt to release up to [size] bytes of non-essential memory currently held by SQLite. An
 * example of non-essential memory is memory used to cache database pages that are not currently in
 * use.
 *
 * [sqlite3_release_memory()](https://sqlite.org/c3ref/release_memory.html)
 */
public expect fun sqlite3_release_memory(size: Int): Int

/**
 * Terminate the current execution of an SQL statement and reset it back to its starting state so
 * that it can be reused. A success code from the prior execution is returned.
 *
 * This routine sets the error code and string returned by [sqlite3_errcode], [sqlite3_errmsg] and
 * sqlite3_errmsg16().
 *
 * [sqlite3_reset()](https://sqlite.org/c3ref/reset.html)
 */
public expect fun sqlite3_reset(stmt: sqlite3_stmt): Sqlite3Result

/**
 * Reset the automatic extension loading mechanism.
 *
 * [sqlite3_reset_auto_extension()](https://sqlite.org/c3ref/reset_auto_extension.html)
 */
public expect fun sqlite3_reset_auto_extension()

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_blob()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_blob(
    context: sqlite3_context,
    bytes: ByteArray,
    size: Int,
    destroy: Sqlite3DestroyCallback<ByteArray>?
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_blob64()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_blob64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    destroy: Sqlite3DestroyCallback<Buffer>?
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_double()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_double(
    context: sqlite3_context,
    value: Double
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_error()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_error(
    context: sqlite3_context,
    message: String
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_error_code()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_error_code(
    context: sqlite3_context,
    errorCode: Int
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_error_nomem()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_error_nomem(context: sqlite3_context)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_error_toobig()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_error_toobig(context: sqlite3_context)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_int()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_int(
    context: sqlite3_context,
    value: Int
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_int64()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_int64(
    context: sqlite3_context,
    value: Long
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_null()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_null(context: sqlite3_context)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_pointer()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun <Data> sqlite3_result_pointer(
    context: sqlite3_context,
    data: Data,
    type: String?,
    destroy: Sqlite3DestroyCallback<Data>?
)

/**
 * Set the subtype of the result from the application-defined SQL function with sqlite3_context
 * [context] to [subtype].
 *
 * [sqlite3_result_subtype()](https://sqlite.org/c3ref/result_subtype.html)
 */
public expect fun sqlite3_result_subtype(
    context: sqlite3_context,
    subtype: UInt
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_text()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_text(
    context: sqlite3_context,
    value: String
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_text64()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_text64(
    context: sqlite3_context,
    buffer: Buffer,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destroy: Sqlite3DestroyCallback<Buffer>?
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_value()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_value(
    context: sqlite3_context,
    value: sqlite3_value,
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_zeroblob()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_zeroblob(
    context: sqlite3_context,
    size: Int
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_zeroblob64()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_zeroblob64(
    context: sqlite3_context,
    size: ULong
): Sqlite3Result

/**
 * Register a callback to be invoked each time a transaction is rolled back by this database
 * connection.
 *
 * [sqlite3_rollback_hook()](https://sqlite.org/c3ref/commit_hook.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * The previous hook is returned by SQLite but is not returned by KSQlite for now.
 */
public expect fun <AppData> sqlite3_rollback_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3RollbackHookCallback<AppData>?
)

/**
 * Return the serialization of a database.
 *
 * [sqlite3_serialize()](https://sqlite.org/c3ref/serialize.html)
 */
public expect fun sqlite3_serialize(
    db: sqlite3,
    schema: String?,
    flags: Sqlite3SerializeFlag?
): Buffer?

/**
 * Set or clear the access authorization function.
 *
 * [sqlite3_set_authorizer()](https://sqlite.org/c3ref/set_authorizer.html)
 */
public expect fun <AppData> sqlite3_set_authorizer(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3AuthorizerCallback<AppData>?
): Sqlite3Result

/**
 * Set the auxiliary data pointer and delete function, for the [index]-th argument to the
 * user-function defined by [context]. Any previous value is deleted by calling the delete function
 * specified when it was set.
 *
 * The left-most argument is 0.
 *
 * Undocumented behavior: If [index] is negative then make the data available to all functions
 * within the current prepared statement using [index] as an access code.
 *
 * [sqlite3_set_auxdata()](https://sqlite.org/c3ref/get_auxdata.html)
 */
public inline fun <reified Data : Any> sqlite3_set_auxdata(
    context: sqlite3_context,
    index: Int,
    data: Data,
    destroy: Sqlite3DestroyCallback<Data>?
) {
    userDataInternal(context)?.setAuxiliaryData(context, index, data, destroy)
}

/**
 * Set the error code and error message associated with the database handle.
 *
 * [sqlite3_set_errmsg()](https://sqlite.org/c3ref/set_errmsg.html)
 */
public expect fun sqlite3_set_errmsg(
    db: sqlite3,
    errorCode: Sqlite3Result.Failure,
    message: String?
): Sqlite3Result

/**
 * Set the value returned by the [sqlite3_last_insert_rowid] API function.
 *
 * [sqlite3_set_last_insert_rowid()](https://sqlite.org/c3ref/set_last_insert_rowid.html)
 */
public expect fun sqlite3_set_last_insert_rowid(
    db: sqlite3,
    rowId: Long
)

/**
 * Undo the effects of sqlite3_initialize(). Must not be called while there are outstanding database
 * connections or memory allocations or while any part of SQLite is otherwise in use in any thread.
 * This  routine is not threadsafe. But it is safe to invoke this routine on when SQLite is already
 * shut down. If SQLite is already shut down when this routine is invoked, then this routine is a
 * harmless no-op.
 *
 * [sqlite3_shutdown()](https://sqlite.org/c3ref/initialize.html)
 */
public expect fun sqlite3_shutdown(): Sqlite3Result

/**
 * Return a string that identifies the specific version of the source code that was used to build
 * the SQLite library.
 *
 * [sqlite3_sourceid()](https://sqlite.org/c3ref/libversion.html)
 */
public expect fun sqlite3_sourceid(): String

/**
 * Return the SQL associated with a prepared statement.
 *
 * [sqlite3_sql()](https://sqlite.org/c3ref/expanded_sql.html)
 */
public expect fun sqlite3_sql(stmt: sqlite3_stmt): String

/**
 * Query status information.
 *
 * [sqlite3_status()](https://sqlite.org/c3ref/status.html)
 */
public expect fun sqlite3_status(
    option: Sqlite3StatusOption,
    outCurrent: Int32OutputParam,
    outHighwater: Int32OutputParam,
    resetFlag: Int
): Sqlite3Result

/**
 * Query status information.
 *
 * [sqlite3_status64()](https://sqlite.org/c3ref/status.html)
 */
public expect fun sqlite3_status64(
    option: Sqlite3StatusOption,
    outCurrent: Int64OutputParam,
    outHighwater: Int64OutputParam,
    resetFlag: Int
): Sqlite3Result

/**
 * Execute the statement [stmt], either until a row of data is ready, the statement is completely
 * executed or an error occurs.
 *
 * [sqlite3_step()](https://sqlite.org/c3ref/step.html)
 */
public expect fun sqlite3_step(stmt: sqlite3_stmt): Sqlite3Result

/**
 * Return true if the prepared statement is in need of being reset.
 *
 * [sqlite3_stmt_busy()](https://sqlite.org/c3ref/stmt_busy.html)
 */
public expect fun sqlite3_stmt_busy(stmt: sqlite3_stmt): Int

/**
 * Set the explain mode for a statement.
 *
 * [sqlite3_stmt_explain()](https://sqlite.org/c3ref/stmt_explain.html)
 */
public expect fun sqlite3_stmt_explain(
    stmt: sqlite3_stmt,
    mode: Sqlite3ExplainMode
): Sqlite3Result

/**
 * Return 1 if the statement is an EXPLAIN and return 2 if the statement is an EXPLAIN QUERY PLAN.
 *
 * [sqlite3_stmt_isexplain()](https://sqlite.org/c3ref/stmt_isexplain.html)
 */
public expect fun sqlite3_stmt_isexplain(stmt: sqlite3_stmt): Sqlite3ExplainMode

/**
 * Return true if the prepared statement is guaranteed to not modify the database.
 *
 * [sqlite3_stmt_readonly()](https://sqlite.org/c3ref/stmt_readonly.html)
 */
public expect fun sqlite3_stmt_readonly(stmt: sqlite3_stmt): Int

/**
 * Return the value of a status counter for a prepared statement.
 *
 * [sqlite3_stmt_status()](https://sqlite.org/c3ref/stmt_status.html)
 */
public expect fun sqlite3_stmt_status(
    stmt: sqlite3_stmt,
    counter: Sqlite3StatementStatusCounter,
    resetFlag: Int
): Int

/**
 * Return 0 on a match (like strcmp()) and  non-zero if there is no match.
 *
 * [sqlite3_strglob()](https://sqlite.org/c3ref/strglob.html)
 */
public expect fun sqlite3_strglob(
    globPattern: String,
    input: String
): Int

/**
 * Allow applications and extensions to compare the contents of two buffers containing UTF-8 strings
 * in a case-independent fashion, using the same definition of "case independence" that SQLite uses
 * internally when comparing identifiers.
 *
 * [sqlite3_stricmp()](https://sqlite.org/c3ref/stricmp.html)
 */
public expect fun sqlite3_stricmp(
    first: String,
    second: String
): Int

/**
 * Return 0 on a match and non-zero for  a miss - like strcmp().
 *
 * [sqlite3_strlike()](https://sqlite.org/c3ref/strlike.html)
 */
public expect fun sqlite3_strlike(
    likePattern: String,
    input: String,
    escapeCharacter: Char
): Int

/**
 * Allow applications and extensions to compare the contents of two buffers containing UTF-8 strings
 * in a case-independent fashion, using the same definition of "case independence" that SQLite uses
 * internally when comparing identifiers.
 *
 * [sqlite3_strnicmp()](https://sqlite.org/c3ref/stricmp.html)
 */
public expect fun sqlite3_strnicmp(
    first: String,
    second: String,
    maxCharacters: Int
): Int

/**
 * Attempt to return the underlying operating system error code or error number that caused the most
 * recent I/O error or failure to open a file. The return value is OS-dependent.
 *
 * [sqlite3_system_errno()](https://sqlite.org/c3ref/system_errno.html)
 */
public expect fun sqlite3_system_errno(db: sqlite3): Int

/**
 * Return meta information about a specific column of a database table.
 *
 * [sqlite3_table_column_metadata()](https://sqlite.org/c3ref/table_column_metadata.html)
 */
public expect fun sqlite3_table_column_metadata(
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
 * Return the number of changes since the database handle was opened.
 *
 * [sqlite3_total_changes()](https://sqlite.org/c3ref/total_changes.html)
 */
public expect fun sqlite3_total_changes(db: sqlite3): Int

/**
 * Return the number of changes since the database handle was opened.
 *
 * [sqlite3_total_changes64()](https://sqlite.org/c3ref/total_changes.html)
 */
public expect fun sqlite3_total_changes64(db: sqlite3): Long

/**
 * Register a trace callback using the version-2 interface.
 *
 * [sqlite3_trace_v2()](https://sqlite.org/c3ref/trace_v2.html)
 */
public expect fun <AppData> sqlite3_trace_v2(
    db: sqlite3,
    mask: Sqlite3TraceCode?,
    appData: AppData,
    callback: Sqlite3TraceCallback<AppData>?
): Sqlite3Result

/**
 * Return the transaction state for a single database, or the maximum transaction state over all
 * attached databases if [schema] is null.
 *
 * [sqlite3_txn_state()](https://sqlite.org/c3ref/txn_state.html)
 */
public expect fun sqlite3_txn_state(
    db: sqlite3,
    schema: String?
): Sqlite3TransactionState?

/**
 * Register a callback to be invoked each time a row is updated, inserted or deleted using this
 * database connection.
 *
 * [sqlite3_update_hook()](https://sqlite.org/c3ref/update_hook.html)
 *
 * -------------------------------------------------------------------------------------------------
 *
 * # Ksqlite
 *
 * The previous hook is returned by SQLite but is not returned by KSQlite for now.
 */
public expect fun <AppData> sqlite3_update_hook(
    db: sqlite3,
    appData: AppData,
    callback: Sqlite3UpdateHookCallback<AppData>?
)

/**
 * Return a boolean value for a query parameter.
 *
 * [sqlite3_uri_boolean()](https://sqlite.org/c3ref/uri_boolean.html)
 */
public expect fun sqlite3_uri_boolean(
    fileName: sqlite3_filename,
    parameter: String,
    default: Int
): Int

/**
 * Return a 64-bit integer value for a query parameter.
 *
 * [sqlite3_uri_int64()](https://sqlite.org/c3ref/uri_boolean.html)
 */
public expect fun sqlite3_uri_int64(
    fileName: sqlite3_filename,
    parameter: String,
    default: Long
): Long

/**
 * Return a pointer to the name of [index]-th query parameter of the filename.
 *
 * [sqlite3_uri_key()](https://sqlite.org/c3ref/uri_boolean.html)
 */
public expect fun sqlite3_uri_key(
    fileName: sqlite3_filename,
    index: Int
): String?

/**
 * This is a utility routine, useful to VFS implementations, that checks to see if a database file
 * was a URI that contained a specific query parameter, and if so obtains the value of the query
 * parameter.
 *
 * The [fileName] argument is the filename pointer passed into the xOpen() method of a VFS
 * implementation. The [parameter] argument is the name of the query parameter we seek. This routine
 * returns the value of the [parameter] parameter if it exists. If the parameter does not exist,
 * this routine returns a NULL pointer.
 *
 * [sqlite3_uri_parameter()](https://sqlite.org/c3ref/uri_boolean.html)
 */
public expect fun sqlite3_uri_parameter(
    fileName: sqlite3_filename,
    parameter: String
): String?

/**
 * Extract the user data from a sqlite3_context structure and return a pointer to it.
 *
 * [sqlite3_user_data()](https://sqlite.org/c3ref/user_data.html)
 */
public inline fun <reified AppData : Any> sqlite3_user_data(context: sqlite3_context): AppData? {
    return castOrThrows(userDataInternal(context)?.appData)
}

/**
 * Extract information from sqlite3_value structure.
 *
 * [sqlite3_value_blob()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_blob(value: sqlite3_value): ByteArray?

/**
 * Variant of [sqlite3_value_blob] that returns a [Buffer] instead.
 */
public fun sqlite3_value_buffer(value: sqlite3_value): ReadableBuffer? =
    valueBufferInternal(value)?.readOnly()

/**
 * Extract information from sqlite3_value structure.
 *
 * [sqlite3_value_bytes()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_bytes(value: sqlite3_value): Int

/**
 * Extract information from sqlite3_value structure.
 *
 * [sqlite3_value_double()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_double(value: sqlite3_value): Double

/**
 * Make a copy of an sqlite3_value object.
 *
 * [sqlite3_value_dup()](https://sqlite.org/c3ref/value_dup.html)
 */
public expect fun sqlite3_value_dup(value: sqlite3_value): sqlite3_value?

/**
 * Return the current text encoding of the [value].
 *
 * [sqlite3_value_encoding()](https://sqlite.org/c3ref/value_encoding.html)
 */
public expect fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2?

/**
 * Free an sqlite3_value object previously obtained from sqlite3_value_dup().
 *
 * [sqlite3_value_free()](https://sqlite.org/c3ref/value_dup.html)
 */
public expect fun sqlite3_value_free(value: sqlite3_value)

/**
 * Return true if a parameter value originated from an sqlite3_bind().
 *
 * [sqlite3_value_frombind()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_frombind(value: sqlite3_value): Int

/**
 * Extract information from sqlite3_value structure.
 *
 * [sqlite3_value_int()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_int(value: sqlite3_value): Int

/**
 * Extract information from sqlite3_value structure.
 *
 * [sqlite3_value_int64()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_int64(value: sqlite3_value): Long

/**
 * Return true if a parameter to xUpdate represents an unchanged column.
 *
 * [sqlite3_value_nochange()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_nochange(value: sqlite3_value): Int

/**
 * Try to convert the type of a function argument or a result column into a numeric representation.
 * Use either INTEGER or REAL whichever is appropriate.  But only do the conversion if it is
 * possible without loss of information and return the revised type of the argument.
 *
 * [sqlite3_value_numeric_type()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_numeric_type(value: sqlite3_value): Sqlite3DataType

/**
 * Extract information from sqlite3_value structure.
 *
 * [sqlite3_value_pointer()](https://sqlite.org/c3ref/value_blob.html)
 */
public inline fun <reified Data> sqlite3_value_pointer(
    value: sqlite3_value,
    type: String?
): Data? = castOrThrows(valuePointerInternal(value, type))

/**
 * Return the subtype for an application-defined SQL function argument [value].
 *
 * [sqlite3_value_subtype()](https://sqlite.org/c3ref/value_subtype.html)
 */
public expect fun sqlite3_value_subtype(value: sqlite3_value): UInt

/**
 * Extract information from sqlite3_value structure.
 *
 * [sqlite3_value_text()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_text(value: sqlite3_value): String?

/**
 * Return the default datatype of the [value].
 *
 * [sqlite3_value_type()](https://sqlite.org/c3ref/value_blob.html)
 */
public expect fun sqlite3_value_type(value: sqlite3_value): Sqlite3DataType

/**
 * Locate a VFS by name. If no name is given, simply return the first VFS on the list.
 *
 * [sqlite3_vfs_find()](https://sqlite.org/c3ref/vfs_find.html)
 */
public expect fun sqlite3_vfs_find(name: String?): sqlite3_vfs?

/**
 * Register a VFS with the system. It is harmless to register the same VFS multiple times. The new
 * VFS becomes the default if [makeDefault] is true.
 *
 * [sqlite3_vfs_register()](https://sqlite.org/c3ref/vfs_find.html)
 */
public expect fun sqlite3_vfs_register(
    vfs: sqlite3_vfs,
    makeDefault: Int
): Sqlite3Result

/**
 * Unregister a VFS so that it is no longer accessible.
 *
 * [sqlite3_vfs_find()](https://sqlite.org/c3ref/vfs_find.html)
 */
public expect fun sqlite3_vfs_unregister(vfs: sqlite3_vfs): Sqlite3Result

/**
 * Return the collating sequence for a constraint passed into xBestIndex.
 *
 * [sqlite3_vtab_collation()](https://sqlite.org/c3ref/vtab_collation.html)
 */
public expect fun sqlite3_vtab_collation(
    info: sqlite3_index_info,
    index: Int
): String?

/**
 * Call from within the xCreate() or xConnect() methods to provide the SQLite core with additional
 * information about the behavior of the virtual table being implemented.
 *
 * [sqlite3_vtab_config()](https://sqlite.org/c3ref/vtab_config.html)
 */
public expect fun sqlite3_vtab_config(
    db: sqlite3,
    option: Sqlite3VTabConfigOption
): Sqlite3Result

/**
 * Return true if ORDER BY clause may be handled as DISTINCT.
 *
 * [sqlite3_vtab_distinct()](https://sqlite.org/c3ref/vtab_distinct.html)
 */
public expect fun sqlite3_vtab_distinct(info: sqlite3_index_info): Int

/**
 *  Return true if constraint iCons is really an IN(...) constraint, or false otherwise. If iCons
 *  is an IN(...) constraint, set (if bHandle!=0) or clear (if bHandle==0) the flag to handle it
 *  using an iterator.
 *
 * [sqlite3_vtab_in()](https://sqlite.org/c3ref/vtab_in.html)
 */
public expect fun sqlite3_vtab_in(
    info: sqlite3_index_info,
    index: Int,
    handle: Int
): Int

/**
 * Set the iterator value [value] to point to the first value in the set.
 * Set [outValue] to point to this value before returning.
 *
 * [sqlite3_vtab_in_first()](https://sqlite.org/c3ref/vtab_in_first.html)
 */
public expect fun sqlite3_vtab_in_first(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result

/**
 * Set the iterator value [value] to point to the next value in the set.
 * Set [outValue] to point to this value before returning.
 *
 * [sqlite3_vtab_in_next()](https://sqlite.org/c3ref/vtab_in_first.html)
 */
public expect fun sqlite3_vtab_in_next(
    value: sqlite3_value,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result

/**
 * If this routine is invoked from within an xColumn method of a virtual table, then it returns true
 * if and only if the call is during an UPDATE operation and the value of the column will not be
 * modified by the UPDATE.
 *
 * If this routine is called from any context other than within the Column method of a virtual
 * table, then the return value is meaningless and arbitrary.
 *
 * Virtual table implements might use this routine to optimize their performance by substituting a
 * NULL result, or some other light-weight value, as a signal to the xUpdate routine that the column
 * is unchanged.
 *
 * [sqlite3_vtab_nochange()](https://sqlite.org/c3ref/vtab_nochange.html)
 */
public expect fun sqlite3_vtab_nochange(context: sqlite3_context): Int

/**
 * Return the ON CONFLICT resolution mode in effect for the virtual table update operation currently
 * in progress.
 *
 * The results of this routine are undefined unless it is called from  within an xUpdate method.
 *
 * [sqlite3_vtab_on_conflict()](https://sqlite.org/c3ref/vtab_on_conflict.html)
 */
public expect fun sqlite3_vtab_on_conflict(db: sqlite3): Sqlite3Result

/**
 * This interface is callable from within the xBestIndex callback only.
 *
 * If possible, set (*ppVal) to point to an object containing the value on the right-hand-side of
 * constraint iCons.
 *
 * [sqlite3_vtab_rhs_value()](https://sqlite.org/c3ref/vtab_rhs_value.html)
 */
public expect fun sqlite3_vtab_rhs_value(
    info: sqlite3_index_info,
    index: Int,
    outValue: Sqlite3ValueOutputParam?
): Sqlite3Result