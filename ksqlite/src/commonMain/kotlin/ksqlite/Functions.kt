@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite

import ksqlite.types.Sqlite3AutoExtensionCallback
import ksqlite.types.Sqlite3Buffer
import ksqlite.types.Sqlite3BusyHandlerCallback
import ksqlite.types.Sqlite3CollationCompareCallback
import ksqlite.types.Sqlite3CollationNeededCallback
import ksqlite.types.Sqlite3CommitHookCallback
import ksqlite.types.Sqlite3CompleteResult
import ksqlite.types.Sqlite3ConfigOption
import ksqlite.types.Sqlite3CreateFunctionFinalCallback
import ksqlite.types.Sqlite3CreateFunctionFuncCallback
import ksqlite.types.Sqlite3CreateFunctionInverseCallback
import ksqlite.types.Sqlite3CreateFunctionStepCallback
import ksqlite.types.Sqlite3CreateFunctionValueCallback
import ksqlite.types.Sqlite3DataType
import ksqlite.types.Sqlite3DbConfigOption
import ksqlite.types.Sqlite3DbStatusOption
import ksqlite.types.Sqlite3DeserializeFlag
import ksqlite.types.Sqlite3DestructorCallback
import ksqlite.types.Sqlite3ExecCallback
import ksqlite.types.Sqlite3IntParam
import ksqlite.types.Sqlite3LongParam
import ksqlite.types.Sqlite3Result
import ksqlite.types.Sqlite3Utf8Param
import ksqlite.types.Sqlite3TextEncoding
import ksqlite.types.sqlite3
import ksqlite.types.sqlite3_context
import ksqlite.types.sqlite3_filename
import ksqlite.types.sqlite3_module
import ksqlite.types.sqlite3_stmt
import ksqlite.types.sqlite3_value

/**
 * Allocate or return the aggregate context for a user function.  A new  context is allocated on the
 * first call. Subsequent calls return the same context that was returned on prior calls.
 *
 * [sqlite3_aggregate_context()](https://sqlite.org/c3ref/aggregate_context.html)
 */
public expect fun sqlite3_aggregate_context(
    context: sqlite3_context,
    nBytes: Int
): Sqlite3Buffer?

/**
 * Register a statically linked extension that is automatically loaded by every new database
 * connection.
 *
 * [sqlite3_auto_extension()](https://sqlite.org/c3ref/auto_extension.html)
 */
public expect fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_blob(
    stmt: sqlite3_stmt,
    index: Int,
    zData: ByteArray?,
    nData: Int
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
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
    zName: String
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
): String

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_pointer(
    stmt: sqlite3_stmt,
    index: Int,
    data: Any?,
    ptrType: String
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_text(
    stmt: sqlite3_stmt,
    index: Int,
    zData: String?,
    nData: Int
): Sqlite3Result

/**
 * This routine sets the busy callback for an SQLite database to the given callback function with
 * the given argument.
 *
 * [sqlite3_busy_handler()](https://sqlite.org/c3ref/busy_handler.html)
 */
public expect fun sqlite3_busy_handler(
    db: sqlite3,
    callback: Sqlite3BusyHandlerCallback?
): Sqlite3Result

/**
 * This routine installs a default busy handler that waits for the specified number of milliseconds
 * before returning 0.
 *
 * [sqlite3_busy_timeout()](https://sqlite.org/c3ref/busy_timeout.html)
 */
public expect fun sqlite3_busy_timeout(
    db: sqlite3,
    ms: Int
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
 * [sqlite3_changes()](https://sqlite.org/c3ref/changes.html)
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
 * [sqlite3_close()](https://sqlite.org/c3ref/close.html)
 */
public expect fun sqlite3_close_v2(db: sqlite3): Sqlite3Result

/**
 * Register a collation sequence factory callback with the database handle [db].
 * Replace any previously installed collation sequence factory.
 *
 * [sqlite3_collation_needed()](https://sqlite.org/c3ref/collation_needed.html)
 */
public expect fun sqlite3_collation_needed(
    db: sqlite3,
    callback: Sqlite3CollationNeededCallback,
): Sqlite3Result

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_blob(
    stmt: sqlite3_stmt,
    index: Int
): ByteArray

/**
 * The following routines are used to access elements of the current row in the result set.
 * Return the ize of a BLOB or a UTF-8 TEXT result in bytes.
 *
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
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
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_double(
    stmt: sqlite3_stmt,
    index: Int
): Double

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_int(
    stmt: sqlite3_stmt,
    index: Int
): Int

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
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
 * [sqlite3_column_database_name()](https://sqlite.org/c3ref/column_database_name.html)
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
 * [sqlite3_column_database_name()](https://sqlite.org/c3ref/column_database_name.html)
 */
public expect fun sqlite3_column_table_name(
    stmt: sqlite3_stmt,
    index: Int
): String?

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_text(
    stmt: sqlite3_stmt,
    index: Int
): String

/**
 * The following routines are used to access elements of the current row in the result set.
 * Return the default datatype of the result.
 *
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_type(
    stmt: sqlite3_stmt,
    index: Int
): Sqlite3DataType

/**
 * The following routines are used to access elements of the current row in the result set.
 *
 * [sqlite3_column_blob()](https://sqlite.org/c3ref/column_blob.html)
 */
public expect fun sqlite3_column_value(
    stmt: sqlite3_stmt,
    index: Int
): sqlite3_value

/**
 * Register a function to be invoked when a transaction commits. If the invoked function returns
 * non-zero, then the commit becomes a rollback.
 *
 * [sqlite_commit_hook()](https://sqlite.org/c3ref/commit_hook.html)
 */
public expect fun sqlite3_commit_hook(
    db: sqlite3,
    callback: Sqlite3CommitHookCallback?
): Sqlite3CommitHookCallback?

/**
 * Return the [index]-th compile-time option string. If [index] is out of range, return `null`.
 *
 * [sqlite3_compileoption_get()](https://sqlite.org/c3ref/compileoption_get.html)
 */
public expect fun sqlite3_compileoption_get(
    db: sqlite3,
    index: Int
): String?

/**
 * Given the name of a compile-time option, return true if that option was used and false if not.
 *
 * The name can optionally begin with "SQLITE_" but the "SQLITE_" prefix is not required for a
 * match.
 *
 * [sqlite3_compileoption_get()](https://sqlite.org/c3ref/compileoption_get.html)
 */
public expect fun sqlite3_compileoption_used(optName: String): Boolean

/**
 * Return `1` if the given SQL string ends in a semicolon.
 *
 * [sqlite3_complete()](https://sqlite.org/c3ref/complete.html)
 */
public expect fun sqlite3_complete(): Sqlite3CompleteResult

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
public expect fun sqlite3_context_db_handle(context: sqlite3_context): sqlite3

/**
 * Register a new collation sequence with the database handle [db].
 *
 * [sqlite3_create_collation()](https://sqlite.org/c3ref/create_collation.html)
 */
public expect fun sqlite3_create_collation(
    db: sqlite3,
    name: String,
    textRep: Sqlite3TextEncoding.Set0,
    callback: Sqlite3CollationCompareCallback?
): Sqlite3Result

/**
 * Register a new collation sequence with the database handle [db].
 *
 * [sqlite3_create_collation()](https://sqlite.org/c3ref/create_collation.html)
 */
public expect fun sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    encoding: Sqlite3TextEncoding.Set0,
    callback: Sqlite3CollationCompareCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result

/**
 * Create new user functions.
 *
 * [sqlite3_create_function()](https://sqlite.org/c3ref/create_function.html)
 */
public expect fun sqlite3_create_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    func: Sqlite3CreateFunctionFuncCallback?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?
): Sqlite3Result

/**
 * Create new user functions.
 *
 * [sqlite3_create_function()](https://sqlite.org/c3ref/create_function.html)
 */
public expect fun sqlite3_create_function_v2(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    func: Sqlite3CreateFunctionFuncCallback?,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result

/**
 * External API function used to create a new virtual-table module.
 *
 * [sqlite3_create_module()](https://sqlite.org/c3ref/create_module.html)
 */
public expect fun sqlite3_create_module(
    db: sqlite3,
    name: String,
    module: sqlite3_module?
): Sqlite3Result

/**
 * External API function used to create a new virtual-table module.
 *
 * [sqlite3_create_module()](https://sqlite.org/c3ref/create_module.html)
 */
public expect fun sqlite3_create_module_v2(
    db: sqlite3,
    name: String,
    module: sqlite3_module?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result

/**
 * Create new user functions.
 *
 * [sqlite3_create_function()](https://sqlite.org/c3ref/create_function.html)
 */
public expect fun sqlite3_create_window_function(
    db: sqlite3,
    name: String,
    nArg: Int,
    encoding: Sqlite3TextEncoding,
    step: Sqlite3CreateFunctionStepCallback?,
    final: Sqlite3CreateFunctionFinalCallback?,
    value: Sqlite3CreateFunctionValueCallback?,
    inverse: Sqlite3CreateFunctionInverseCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result

/**
 * Return the number of values available from the current row of the currently executing statement
 * [stmt].
 *
 * [sqlite3_data_count()](https://sqlite.org/c3ref/data_count.html)
 */
public expect fun sqlite3_data_count(stmt: sqlite3_stmt): Int

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
): sqlite3_filename

/**
 * Return the sqlite3* database handle to which the prepared statement given in the argument
 * belongs. This is the same database handle that was the first argument to the [sqlite3_prepare]
 * that was used to create the statement in the first place.
 *
 * [sqlite3_db_handle()](https://sqlite.org/c3ref/db_handle.html)
 */
public expect fun sqlite3_db_handle(stmt: sqlite3_stmt): sqlite3

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
 * 32-bit variant of [sqlite3_db_status64].
 *
 * [sqlite3_db_status()](https://sqlite.org/c3ref/db_status.html)
 */
public expect fun sqlite3_db_status(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    current: Sqlite3IntParam,
    highwtr: Sqlite3IntParam,
    resetFlag: Boolean
): Sqlite3Result

/**
 * Query status information for a single database connection.
 *
 * [sqlite3_db_status()](https://sqlite.org/c3ref/db_status.html)
 */
public expect fun sqlite3_db_status64(
    db: sqlite3,
    option: Sqlite3DbStatusOption,
    current: Sqlite3LongParam,
    highwtr: Sqlite3LongParam,
    resetFlag: Boolean
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
    data: ByteArray,
    szDb: Long,
    szBuf: Long,
    mFlags: Sqlite3DeserializeFlag?
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
 * Return the most recent error code generated by an SQLite routine. If NULL is passed to this
 * function, we assume a malloc() failed during sqlite3_open().
 *
 * [sqlite3_errcode()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_extended_errcode(db: sqlite3): Int

/**
 * Return UTF-8 encoded English language explanation of the most recent error.
 *
 * [sqlite3_errcode()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_errmsg(db: sqlite3): String?

/**
 * Return a string that describes the kind of error specified in the argument.
 *
 * [sqlite3_errcode()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_errstr(resultCode: Int): String?

/**
 * Return the byte offset of the most recent error.
 *
 * [sqlite3_errcode()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_error_offset(): Int

/**
 * Execute SQL code. Return one of the SQLITE_ success/failure codes. Also write an error message
 * into memory obtained from malloc() and make [errMsg] point to that message.
 *
 * If the SQL is a query, then for each row in the query result the [callback] function is called.
 * If [callback]=`null` then no callback is invoked, even for queries.
 *
 * [sqlite3_exec()](https://sqlite.org/c3ref/exec.html)
 */
public expect fun sqlite3_exec(
    db: sqlite3,
    sql: String,
    callback: Sqlite3ExecCallback?,
    errMsg: Sqlite3Utf8Param?
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
 * [sqlite3_errcode()](https://sqlite.org/c3ref/errcode.html)
 */
public expect fun sqlite3_extended_errcode(db: sqlite3): Int