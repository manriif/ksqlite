@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.Struct
import ksqlite.capi.vfs.callbacks.SqliteVfsAccessCallback
import ksqlite.capi.vfs.callbacks.SqliteVfsDeleteCallback
import ksqlite.capi.vfs.callbacks.SqliteVfsOpenCallback
import ksqlite.types.SqliteAccessFlag
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode
import ksqlite.types.vfs.SqliteVfs
import ksqlite.types.vfs.SqliteVfsVersion
import ksqlite.capi.memory.OutputParam as MemoryOutputParam

/**
 * An instance of the sqlite3_vfs object defines the interface between the SQLite core and the
 * underlying operating system. The "vfs" in the name of the object stands for "virtual file
 * system". See the VFS documentation for further information.
 *
 * [sqlite3_vfs](https://sqlite.org/c3ref/vfs.html)
 */
public expect class sqlite3_vfs : Struct, SqliteVfs {

    override val iVersion: SqliteVfsVersion
    override val szOsFile: Int
    override val mxPathname: Int
    override val zName: String

    public val xOpen: SqliteVfsOpenCallback
    public val xDelete: SqliteVfsDeleteCallback
    public val xAccess: SqliteVfsAccessCallback

    /**
     * Output parameter that accepts an [sqlite3_vfs] struct.
     */
    public class OutputParam() : MemoryOutputParam<sqlite3_vfs?> {
        override val value: sqlite3_vfs?
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Applies [xOpen] with supplied arguments.
 */
public fun sqlite3_vfs.xOpen(
    name: String?,
    file: sqlite3_file,
    flags: SqliteOpenFlag.Vfs,
    outFlags: SqliteVfsOpenFlagsOutputParam?
): SqliteResultCode = xOpen.apply(this, name, file, flags, outFlags)

/**
 * Applies [xDelete] with supplied arguments.
 */
public fun sqlite3_vfs.xDelete(
    name: String,
    syncDir: Int
): SqliteResultCode = xDelete.apply(this, name, syncDir)

/**
 * Applies [xAccess] with supplied arguments.
 */
public fun sqlite3_vfs.xAccess(
    name: String,
    flags: SqliteAccessFlag,
    outFlags: Int32OutputParam?
): SqliteResultCode = xAccess.apply(this, name, flags, outFlags)