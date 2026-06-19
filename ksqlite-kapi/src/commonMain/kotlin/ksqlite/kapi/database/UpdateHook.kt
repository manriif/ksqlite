package ksqlite.kapi.database

import ksqlite.types.SqliteActionCode

/**
 * Callback to use with [DatabaseConnection.setUpdateHook].
 */
public fun interface UpdateHook {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/update_hook.html).
     */
    public fun apply(
        action: SqliteActionCode.Dml,
        databaseName: String,
        tableName: String,
        rowid: Long
    )
}