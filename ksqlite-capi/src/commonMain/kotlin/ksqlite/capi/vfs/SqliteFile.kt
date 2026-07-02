@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.AllocatableStruct
import ksqlite.types.vfs.SqliteFile

/**
 * An sqlite3_file object represents an open file in the OS interface layer. Individual OS interface
 * implementations will want to subclass this object by appending additional fields for their own
 * use. The pMethods entry is a pointer to an sqlite3_io_methods object that defines methods for
 * performing I/O operations on the open file.
 *
 * [sqlite3_file](https://sqlite.org/c3ref/file.html)
 */
public expect class sqlite3_file(vfs: sqlite3_vfs) : AllocatableStruct, SqliteFile {

    public val pMethods: sqlite3_io_methods?
}