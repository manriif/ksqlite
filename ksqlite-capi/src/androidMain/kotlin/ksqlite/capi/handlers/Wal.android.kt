package ksqlite.capi.handlers

import ksqlite.foreign.callbacks.WalHookCallback
import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_wal_hook].
 */
internal class WalHookHandler<AppData> :
    Handler<SqliteWalHookCallback<AppData>, AppData>(),
    WalHookCallback {

    override fun apply(
        db: Long,
        dbName: String,
        nPage: Int
    ): Int = handle { callback, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            dbName = dbName,
            nPage = nPage
        ).code
    }
}