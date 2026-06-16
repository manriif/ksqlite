package ksqlite.kapi.connection

import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.kapi.requireConnection

/**
 * Invokes [AutovacuumPages.apply].
 */
internal val AutovacuumPagesCallback = SqliteAutovacuumPagesCallback { callback: AutovacuumPages,
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
internal val BusyHandlerCallback = SqliteBusyHandlerCallback { callback: BusyHandler, count ->
    callback.apply(count)
}

/**
 * Invokes [CollationNeeded.apply].
 */
internal val CollationNeededCallback = SqliteCollationNeededCallback { callback: CollationNeeded,
                                                                       db,
                                                                       eTextRep,
                                                                       name ->
    callback.apply(
        connection = requireConnection(db),
        encoding = eTextRep,
        name = name
    )
}
/*
/**
 * Invokes [BusyHandler.apply].
 */
internal val BusyHandlerCallback = SqliteBusyHandlerCallback { callback: BusyHandler, count ->
    callback.apply(count)
}

/**
 * Invokes [BusyHandler.apply].
 */
internal val BusyHandlerCallback = SqliteBusyHandlerCallback { callback: BusyHandler, count ->
    callback.apply(count)
}

/**
 * Invokes [BusyHandler.apply].
 */
internal val BusyHandlerCallback = SqliteBusyHandlerCallback { callback: BusyHandler, count ->
    callback.apply(count)
}

/**
 * Invokes [BusyHandler.apply].
 */
internal val BusyHandlerCallback = SqliteBusyHandlerCallback { callback: BusyHandler, count ->
    callback.apply(count)
}

/**
 * Invokes [BusyHandler.apply].
 */
internal val BusyHandlerCallback = SqliteBusyHandlerCallback { callback: BusyHandler, count ->
    callback.apply(count)
}*/