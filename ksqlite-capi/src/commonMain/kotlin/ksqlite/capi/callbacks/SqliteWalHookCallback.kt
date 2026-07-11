package ksqlite.capi.callbacks

import ksqlite.capi.sqlite3
import ksqlite.types.SqliteResultCode

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
        databaseName: String,
        pageCount: Int
    ): SqliteResultCode.OkOrFailure
}