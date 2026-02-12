@file:Suppress("ClassName")

package ksqlite

///////////////////////////////////////////////////////////////////////////
// Core
///////////////////////////////////////////////////////////////////////////

/**
 * Generic pointer.
 */
public expect class pointer

///////////////////////////////////////////////////////////////////////////
// Sqlite-specific
///////////////////////////////////////////////////////////////////////////

/**
 * Each open SQLite database is represented by a pointer to an instance of the opaque structure
 * named "sqlite3". It is useful to think of an sqlite3 pointer as an object. The [sqlite3_open],
 * [sqlite3_open16], and [sqlite3_open_v2] interfaces are its constructors, and [sqlite3_close]
 * and [sqlite3_close_v2] are its destructors.
 *
 * [sqlite3](https://sqlite.org/c3ref/sqlite3.html)
 */
public expect class sqlite3

/**
 * A pointer to the opaque sqlite3_api_routines structure is passed as the third parameter to entry
 * points of loadable extensions.
 *
 * [sqlite3_api_routines](https://sqlite.org/c3ref/api_routines.html)
 */
public expect class sqlite3_api_routines

/**
 * The context in which an SQL function executes is stored in an sqlite3_context object. A pointer
 * to an sqlite3_context object is always the first parameter to application-defined SQL functions.
 * The application-defined SQL function implementation will pass this pointer through into calls to
 * [sqlite3_result], [sqlite3_aggregate_context], [sqlite3_user_data], [sqlite3_context_db_handle],
 * [sqlite3_get_auxdata], and/or [sqlite3_set_auxdata].
 *
 * [sqlite3_context](https://sqlite.org/c3ref/context.html)
 */
public expect class sqlite3_context

/**
 * The [sqlite3_backup] object records state information about an ongoing online backup operation.
 * The [sqlite3_backup] object is created by a call to [sqlite3_backup_init] and is destroyed by a
 * call to [sqlite3_backup_finish].
 *
 * [sqlite3_backup](https://sqlite.org/c3ref/backup.html)
 */
public expect class sqlite3_backup

/**
 * An instance of this object represents a single SQL statement that has been compiled into binary
 * form and is ready to be evaluated.
 *
 * Think of each SQL statement as a separate computer program. The original SQL text is source code.
 * A prepared statement object is the compiled object code. All SQL must be converted into a
 * prepared statement before it can be run.
 *
 * [sqlite3_stmt](https://sqlite.org/c3ref/stmt.html)
 */
public expect class sqlite3_stmt