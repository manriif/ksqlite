package ksqlite.kapi.callbacks

import ksqlite.capi.callbacks.Sqlite3AutovacuumPagesCallback
import ksqlite.capi.callbacks.Sqlite3BusyHandlerCallback

/**
 * Invokes [AutovacuumPages.apply].
 */
internal val AutovacuumPagesInvoker = Sqlite3AutovacuumPagesCallback { callback: AutovacuumPages,
                                                                       schemaName,
                                                                       dbPage,
                                                                       freePage,
                                                                       bytePerPage ->
    callback.apply(
        schemaName = schemaName,
        dbPage = dbPage,
        freePage = freePage,
        bytePerPage = bytePerPage
    )
}

/**
 * Invokes [BusyHandler.apply].
 */
internal val BusyHandlerInvoker = Sqlite3BusyHandlerCallback { callback: BusyHandler, count ->
    callback.apply(count)
}