package ksqlite.kapi.database

import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.kapi.sqliteRequireConnection

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
        connection = sqliteRequireConnection(db),
        encoding = eTextRep,
        name = name
    )
}

/**
 * Invokes [CommitHook.apply].
 */
internal val CommitHookCallback = SqliteCommitHookCallback { callback: CommitHook ->
    if (callback.apply()) 1 else 0
}

/**
 * Invokes [Collation.apply].
 */
internal val CollationCallback = SqliteCollationCallback { callback: Collation, lhs, rhs ->
    callback.apply(lhs, rhs)
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
}*/