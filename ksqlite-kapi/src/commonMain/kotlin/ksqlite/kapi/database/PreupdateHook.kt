package ksqlite.kapi.database

import ksqlite.types.SqliteActionCode

/**
 * Callback to use with [DatabaseConnection.setPreupdateHook].
 */
public fun interface PreupdateHook {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/preupdate_blobwrite.html).
     */
    public fun PreupdateHookScope.apply(
        connection: DatabaseConnection,
        action: SqliteActionCode.Dml,
        databaseName: String,
        tableName: String,
        oldRowid: Long,
        newRowid: Long
    )
}