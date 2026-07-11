package ksqlite.kapi.database

import ksqlite.kapi.SQLiteException

/**
 * Callback to use with [WriteAheadLog.setHook].
 */
public fun interface WriteAheadLogHook {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/wal_hook.html).
     *
     * If an error is detected, then an [SQLiteException] must be thrown.
     */
    public fun apply(
        connection: DatabaseConnection,
        databaseName: String,
        pageCount: Int
    )
}