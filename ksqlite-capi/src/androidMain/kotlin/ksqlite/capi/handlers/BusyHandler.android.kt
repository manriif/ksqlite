package ksqlite.capi.handlers

import ksqlite.foreign.callbacks.BusyHandlerCallback
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback

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