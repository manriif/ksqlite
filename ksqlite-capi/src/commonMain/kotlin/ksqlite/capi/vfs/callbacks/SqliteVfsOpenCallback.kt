package ksqlite.capi.vfs.callbacks

import ksqlite.capi.vfs.SqliteVfsOpenFlagsOutputParam
import ksqlite.capi.vfs.sqlite3_file
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteResultCode

/**
 * Represents the [sqlite3_vfs.xOpen] callback.
 */
public fun interface SqliteVfsOpenCallback {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/io_methods.html).
     */
    public fun apply(
        vfs: sqlite3_vfs,
        fileName: String,
        file: sqlite3_file,
        flags: SqliteOpenFlag.Vfs,
        outFlags: SqliteVfsOpenFlagsOutputParam?
    ): SqliteResultCode
}