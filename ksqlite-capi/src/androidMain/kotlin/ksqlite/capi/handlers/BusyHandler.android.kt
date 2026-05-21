package ksqlite.capi.handlers

import ksqlite.BusyHandlerCallback
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler(holder: Holder<Sqlite3BusyHandlerCallback>) :
    Handler<Sqlite3BusyHandlerCallback>(holder),
    BusyHandlerCallback {

    override fun call(n: Int): Int = handler { callback, userData ->
        callback(userData, n)
    }
}