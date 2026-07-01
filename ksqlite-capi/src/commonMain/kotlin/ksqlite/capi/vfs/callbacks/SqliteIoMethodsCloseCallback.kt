package ksqlite.capi.vfs.callbacks

import ksqlite.capi.vfs.sqlite3_file
import ksqlite.capi.vfs.sqlite3_io_methods
import ksqlite.types.SqliteResultCode

/**
 * Represents the [sqlite3_io_methods.xClose] callback.
 */
public fun interface SqliteIoMethodsCloseCallback {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/io_methods.html).
     */
    public fun apply(file: sqlite3_file): SqliteResultCode
}