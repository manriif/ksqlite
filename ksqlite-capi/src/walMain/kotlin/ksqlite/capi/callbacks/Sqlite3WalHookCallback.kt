package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3

/**
 * Callback to use with [ksqlite.capi.sqlite3_wal_hook].
 */
public fun interface Sqlite3WalHookCallback<ClientData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/wal_hook.html).
     */
    public fun handle(
        clientData: ClientData,
        db: sqlite3,
        dbName: String,
        nPage: Int
    ): Sqlite3Result.OkOrFailure
}