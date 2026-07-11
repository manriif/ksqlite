package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.foreign.callbacks.BusyHandlerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler<AppData> :
    Handler<SqliteBusyHandlerCallback<AppData>, AppData>(),
    BusyHandlerCallback {

    override fun apply(n: Int): Int = handle { callback, appData ->
        callback.apply(appData, n)
    }
}