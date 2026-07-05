package ksqlite.capi.vfs.callbacks

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.types.SqliteAccessFlag
import ksqlite.types.SqliteResultCode

/**
 * Represents the [sqlite3_vfs.xAccess] callback.
 */
public fun interface SqliteVfsAccessCallback {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/vfs.html).
     */
    public fun apply(
        vfs: sqlite3_vfs,
        name: String,
        flags: SqliteAccessFlag,
        outFlags: Int32OutputParam?
    ): SqliteResultCode
}