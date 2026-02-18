@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite

import ksqlite.types.Sqlite3AutoExtensionCallback
import ksqlite.types.Sqlite3BusyHandlerCallback
import ksqlite.types.Sqlite3CollationCompareCallback
import ksqlite.types.Sqlite3CollationNeededCallback
import ksqlite.types.Sqlite3CommitHookCallback
import ksqlite.types.Sqlite3ConfigOption
import ksqlite.types.Sqlite3DataType
import ksqlite.types.Sqlite3TextEncoding
import ksqlite.types.pointer
import ksqlite.types.sqlite3
import ksqlite.types.sqlite3_context
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
): pointer?

/**
 * Register a statically linked extension that is automatically loaded by every new database
 * connection.
 *
 * [sqlite3_auto_extension()](https://sqlite.org/c3ref/auto_extension.html)
 */
public expect fun sqlite3_auto_extension(callback: Sqlite3AutoExtensionCallback): Int

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
): Int

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_double(
    stmt: sqlite3_stmt,
    index: Int,
    value: Double
): Int

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_int(
    stmt: sqlite3_stmt,
    index: Int,
    value: Int
): Int

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_int64(
    stmt: sqlite3_stmt,
    index: Int,
    value: Long
): Int

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_null(
    stmt: sqlite3_stmt,
    index: Int
): Int

/**
 * Return the number of wildcards that can be potentially bound to.
 *
 * [sqlite3_bind_parameter_count()](https://sqlite.org/c3ref/bind_parameter_count.html)
 */
public expect fun sqlite3_bind_parameter_count(
    stmt: sqlite3_stmt
)

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
): Int

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
): Int

/**
 * This routine sets the busy callback for an SQLite database to the given callback function with
 * the given argument.
 *
 * [sqlite3_busy_handler()](https://sqlite.org/c3ref/busy_handler.html)
 */
public expect fun sqlite3_busy_handler(
    db: sqlite3,
    callback: Sqlite3BusyHandlerCallback?
): Int

/**
 * This routine installs a default busy handler that waits for the specified number of milliseconds
 * before returning 0.
 *
 * [sqlite3_busy_timeout()](https://sqlite.org/c3ref/busy_timeout.html)
 */
public expect fun sqlite3_busy_timeout(
    db: sqlite3,
    ms: Int
)

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
public expect fun sqlite3_clear_bindings(stmt: sqlite3_stmt): Int

/**
 * Two variations on the public interface for closing a database.
 *
 * The sqlite3_close() version returns SQLITE_BUSY and leaves the connection open if there are
 * unfinalized prepared statements or unfinished sqlite3_backups.
 *
 * [sqlite3_close()](https://sqlite.org/c3ref/close.html)
 */
public expect fun sqlite3_close(db: sqlite3): Int

/**
 * Two variations on the public interface for closing a database.
 *
 * The sqlite3_close_v2() version forces the connection to become a zombie if there are unclosed
 * resources, and arranges for deallocation when the last prepare statement or sqlite3_backup
 * closes.
 *
 * [sqlite3_close()](https://sqlite.org/c3ref/close.html)
 */
public expect fun sqlite3_close_v2(db: sqlite3): Int

/**
 * Register a collation sequence factory callback with the database handle [db].
 * Replace any previously installed collation sequence factory.
 *
 * [sqlite3_collation_needed()](https://sqlite.org/c3ref/collation_needed.html)
 */
public expect fun sqlite3_collation_needed(
    db: sqlite3,
    callback: Sqlite3CollationNeededCallback,
): Int

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
    callback: Sqlite3CommitHookCallback
)

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
public expect fun sqlite3_compileoption_used(optName: String): Int

/**
 * Return `1` if the given SQL string ends in a semicolon.
 *
 * [sqlite3_complete()](https://sqlite.org/c3ref/complete.html)
 */
public expect fun sqlite3_complete(): Int

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
public expect fun sqlite3_config(option: Sqlite3ConfigOption): Int

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
    textRep: Sqlite3TextEncoding,
    callback: Sqlite3CollationCompareCallback?
): Int

/**
 * Register a new collation sequence with the database handle [db].
 *
 * [sqlite3_create_collation()](https://sqlite.org/c3ref/create_collation.html)
 */
public expect fun sqlite3_create_collation_v2(
    db: sqlite3,
    name: String,
    textRep: Sqlite3TextEncoding,
    callback: Sqlite3CollationCompareCallback?
): Int