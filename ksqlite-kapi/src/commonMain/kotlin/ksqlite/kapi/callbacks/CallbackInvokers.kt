package ksqlite.kapi.callbacks

import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback

/**
 * Invokes [AutovacuumPages.apply].
 */
internal val AutovacuumPagesInvoker = SqliteAutovacuumPagesCallback { callback: AutovacuumPages,
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
internal val BusyHandlerInvoker = SqliteBusyHandlerCallback { callback: BusyHandler, count ->
    callback.apply(count)
}