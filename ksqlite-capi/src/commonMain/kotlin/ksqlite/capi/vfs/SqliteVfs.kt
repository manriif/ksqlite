@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.Int32TransformOutputParam
import ksqlite.capi.memory.Struct
import ksqlite.capi.vfs.callbacks.SqliteVfsOpenCallback
import ksqlite.types.SqliteOpenFlag
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

    /**
     * Output parameter that accepts an [sqlite3_vfs] struct.
     */
    public class OutputParam() : MemoryOutputParam<sqlite3_vfs?> {
        override val value: sqlite3_vfs?
    }
}

/**
 * Output parameter that accepts an [SqliteOpenFlag.Vfs].
 */
public class SqliteVfsOpenFlagsOutputParam : Int32TransformOutputParam<SqliteOpenFlag.Vfs>() {

    override fun transform(value: Int): SqliteOpenFlag.Vfs = SqliteOpenFlag.Vfs.from(value)
}