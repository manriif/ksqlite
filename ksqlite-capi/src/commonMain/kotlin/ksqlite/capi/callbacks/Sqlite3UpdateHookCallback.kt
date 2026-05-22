package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3ActionCode

/**
 * Callback to use with [ksqlite.capi.sqlite3_update_hook].
 */
public fun interface Sqlite3UpdateHookCallback<ClientData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/update_hook.html).
     */
    public fun handle(
        clientData: ClientData,
        action: Sqlite3ActionCode.Dml,
        dbName: String,
        tableName: String,
        rowId: Long
    )
}