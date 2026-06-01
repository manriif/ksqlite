package ksqlite.capi.handlers

import ksqlite.WalHookCallback
import ksqlite.capi.callbacks.Sqlite3WalHookCallback
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_wal_hook].
 */
internal class WalHookHandler<AppData> :
    Handler<Sqlite3WalHookCallback<AppData>, AppData>(),
    WalHookCallback {

    override fun call(
        db: Long,
        dbName: String,
        nPage: Int
    ): Int = handle { callback, appData ->
        callback.handle(
            appData = appData,
            db = sqlite3(db),
            dbName = dbName,
            nPage = nPage
        ).code
    }
}