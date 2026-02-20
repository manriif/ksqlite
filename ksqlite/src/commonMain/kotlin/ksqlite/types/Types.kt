@file:Suppress("ClassName")

package ksqlite.types

/**
 * Generic pointer.
 */
public expect class pointer

/**
 * Each open SQLite database is represented by a pointer to an instance of the opaque structure
 * named "sqlite3". It is useful to think of an sqlite3 pointer as an object.
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
 *
 * [sqlite3_context](https://sqlite.org/c3ref/context.html)
 */
public expect class sqlite3_context

/**
 * Type sqlite3_filename is used by SQLite to pass filenames to the xOpen method of a VFS. It may
 * be cast to (const char*) and treated as a normal, nul-terminated, UTF-8 buffer containing the
 * filename, but may also be passed to special APIs.
 *
 * [sqlite3_filename](https://sqlite.org/c3ref/filename.html)
 */
public typealias sqlite3_filename = String

/**
 * This structure, sometimes called a "virtual table module", defines the implementation of a
 * virtual table. This structure consists mostly of methods for the module.
 *
 * [sqlite3_module](https://sqlite.org/c3ref/module.html)
 */
public expect class sqlite3_module

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

/**
 * SQLite uses the sqlite3_value object to represent all values that can be stored in a database
 * table. SQLite uses dynamic typing for the values it stores. Values stored in sqlite3_value
 * objects can be integers, floating point values, strings, BLOBs, or NULL.
 *
 * [sqlite3_value](https://sqlite.org/c3ref/value.html)
 */
public expect class sqlite3_value