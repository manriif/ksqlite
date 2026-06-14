package ksqlite.capi.callbacks

import ksqlite.types.SqliteResultCode
import ksqlite.capi.types.sqlite3

/**
 * Callback to use with [ksqlite.capi.sqlite3_wal_hook].
 */
public fun interface SqliteWalHookCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/wal_hook.html).
     */
    public fun apply(
        appData: AppData,
        db: sqlite3,
        dbName: String,
        nPage: Int
    ): SqliteResultCode.OkOrFailure
}