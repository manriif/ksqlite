package ksqlite.kapi.database

import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteExecCallback
import ksqlite.kapi.helpers.ksqliteLog
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

/**
 * Invokes [Exec.apply].
 */
internal val ExecCallback = SqliteExecCallback { callback: Exec, count, values, names ->
    try {
        with(callback) {
            ExecScopeImpl.apply(
                columnCount = count,
                columnValues = values,
                columnNames = names
            )
        }

        0
    } catch (_: ExecAbortException) {
        1
    } catch (unexpected: Throwable) {
        ksqliteLog(unexpected)
        throw unexpected
    }
}