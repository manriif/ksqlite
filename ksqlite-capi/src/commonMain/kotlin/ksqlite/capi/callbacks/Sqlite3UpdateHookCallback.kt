package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3ActionCode

/**
 * Callback to use with [ksqlite.capi.sqlite3_update_hook].
 */
public fun interface Sqlite3UpdateHookCallback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/update_hook.html).
     */
    public fun handle(
        appData: AppData,
        action: Sqlite3ActionCode.Dml,
        dbName: String,
        tableName: String,
        rowId: Long
    )
}