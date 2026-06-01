package ksqlite.capi.handlers

import ksqlite.BusyHandlerCallback
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler<AppData> :
    Handler<Sqlite3BusyHandlerCallback<AppData>, AppData>(),
    BusyHandlerCallback {

    override fun call(n: Int): Int = handle { callback, appData ->
        callback.apply(appData, n)
    }
}