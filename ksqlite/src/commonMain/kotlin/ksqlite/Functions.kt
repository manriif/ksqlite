@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite

///////////////////////////////////////////////////////////////////////////
// Obtain Aggregate Function Context
///////////////////////////////////////////////////////////////////////////

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

///////////////////////////////////////////////////////////////////////////
// Automatically Load Statically Linked Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Register a statically linked extension that is automatically loaded by every new database
 * connection.
 *
 * [sqlite3_auto_extension()](https://sqlite.org/c3ref/auto_extension.html)
 */
public expect fun sqlite3_auto_extension(
    xEntryPoint: (
        db: sqlite3,
        pzErrMsg: String,
        pThunk: sqlite3_api_routines
    ) -> Int
): Int

///////////////////////////////////////////////////////////////////////////
// Binding Values To Prepared Statements
///////////////////////////////////////////////////////////////////////////

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
public expect fun sqlite3_bind_parameter_count(stmt: sqlite3_stmt)

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
public expect fun sqlite3_bind_parameter_name(stmt: sqlite3_stmt, index: Int): String

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