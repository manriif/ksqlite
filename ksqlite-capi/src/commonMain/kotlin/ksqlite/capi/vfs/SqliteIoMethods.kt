@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.Struct
import ksqlite.capi.vfs.callbacks.SqliteIoMethodsCloseCallback
import ksqlite.types.SqliteResultCode
import ksqlite.types.vfs.SqliteIoMethods
import ksqlite.types.vfs.SqliteIoMethodsVersion

/**
 * Every file opened by the sqlite3_vfs.xOpen method populates an sqlite3_file object (or, more
 * commonly, a subclass of the sqlite3_file object) with a pointer to an instance of this object.
 * This object defines the methods used to perform various operations against the open file
 * represented by the sqlite3_file object.
 *
 * [sqlite3_io_methods](https://sqlite.org/c3ref/io_methods.html)
 */
public expect class sqlite3_io_methods : Struct, SqliteIoMethods {

    override val iVersion: SqliteIoMethodsVersion

    public val xClose: SqliteIoMethodsCloseCallback
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Applies [xClose] with supplied arguments.
 */
public fun sqlite3_io_methods.xClose(file: sqlite3_file): SqliteResultCode = xClose.apply(file)