@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct

/**
 * Each open SQLite database is represented by a pointer to an instance of the opaque structure
 * named "sqlite3". It is useful to think of an sqlite3 pointer as an object.
 *
 * [sqlite3](https://sqlite.org/c3ref/sqlite3.html)
 */
public expect class sqlite3 : Struct, MemoryScope

/**
 * The [sqlite3_backup] object records state information about an ongoing online backup operation.
 * The [sqlite3_backup] object is created by a call to [ksqlite.capi.sqlite3_backup_init] and is
 * destroyed by a call to [ksqlite.capi.sqlite3_backup_finish].
 *
 * [sqlite3_backup](https://sqlite.org/c3ref/backup.html)
 */
public expect class sqlite3_backup : Struct

/**
 * An instance of this object represents an open BLOB on which incremental BLOB I/O can be
 * performed.
 * Objects of this type are created by [ksqlite.capi.sqlite3_blob_open] and destroyed by
 * [ksqlite.capi.sqlite3_blob_close].
 * The [ksqlite.capi.sqlite3_blob_read] and [ksqlite.capi.sqlite3_blob_write] interfaces can be used to read
 * or write small subsections of the BLOB.
 * The [ksqlite.capi.sqlite3_blob_bytes] interface returns the size of the BLOB in bytes.
 *
 * [sqlite3_blob](https://sqlite.org/c3ref/blob.html)
 */
public expect class sqlite3_blob : Struct

/**
 * The context in which an SQL function executes is stored in an sqlite3_context object. A pointer
 * to an sqlite3_context object is always the first parameter to application-defined SQL functions.
 *
 * [sqlite3_context](https://sqlite.org/c3ref/context.html)
 */
public expect class sqlite3_context : Struct

/**
 * Type sqlite3_filename is used by SQLite to pass filenames to the xOpen method of a VFS. It may
 * be cast to (const char*) and treated as a normal, nul-terminated, UTF-8 buffer containing the
 * filename, but may also be passed to special APIs.
 *
 * [sqlite3_filename](https://sqlite.org/c3ref/filename.html)
 */
public typealias sqlite3_filename = String

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
public expect class sqlite3_stmt : Struct, MemoryScope

/**
 * SQLite uses the sqlite3_value object to represent all values that can be stored in a database
 * table. SQLite uses dynamic typing for the values it stores. Values stored in sqlite3_value
 * objects can be integers, floating point values, strings, BLOBs, or NULL.
 *
 * [sqlite3_value](https://sqlite.org/c3ref/value.html)
 */
public expect class sqlite3_value : Struct

/**
 * An instance of the sqlite3_vfs object defines the interface between the SQLite core and the
 * underlying operating system. The "vfs" in the name of the object stands for "virtual file
 * system". See the VFS documentation for further information.
 *
 * [sqlite3_vfs](https://sqlite.org/c3ref/vfs.html)
 */
public expect class sqlite3_vfs : Struct