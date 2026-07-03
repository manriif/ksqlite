package ksqlite.capi.vfs.callbacks

import ksqlite.capi.vfs.sqlite3_vfs
import ksqlite.types.SqliteResultCode

/**
 * Represents the [sqlite3_vfs.xDelete] callback.
 */
public fun interface SqliteVfsDeleteCallback {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/vfs.html).
     */
    public fun apply(
        vfs: sqlite3_vfs,
        name: String,
        syncDir: Int,
    ): SqliteResultCode
}