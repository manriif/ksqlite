package ksqlite.kapi.database

import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteExecCallback
import ksqlite.capi.callbacks.SqlitePreupdateHookCallback
import ksqlite.capi.callbacks.SqliteProgressHandlerCallback
import ksqlite.capi.callbacks.SqliteRollbackHookCallback
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
    if (callback.apply(count, columnValues = values, columnNames = names)) 1 else 0
}

/**
 * Invokes [PreupdateHook.apply].
 */
internal val PreupdateHookCallback = SqlitePreupdateHookCallback { callback: PreupdateHook,
                                                                   db,
                                                                   action,
                                                                   databaseName,
                                                                   tableName,
                                                                   oldRowid,
                                                                   newRowid ->
    callback.run {
        PreupdateHookScopeImpl(db).use { scope ->
            scope.apply(
                connection = sqliteRequireConnection(db),
                action = action,
                databaseName = databaseName,
                tableName = tableName,
                oldRowid = oldRowid,
                newRowid = newRowid
            )
        }
    }
}

/**
 * Invokes [ProgressHandler.apply].
 */
internal val ProgressHandlerCallback = SqliteProgressHandlerCallback { callback: ProgressHandler ->
    if (callback.apply()) 1 else 0
}

/**
 * Invokes [RollbackHook.apply].
 */
internal val RollbackHookCallback = SqliteRollbackHookCallback(RollbackHook::apply)

/**
 * Invokes [Authorizer.apply].
 */
internal val AuthorizerCallback = SqliteAuthorizerCallback { callback: Authorizer,
                                                             action,
                                                             detail1,
                                                             detail2,
                                                             detail3,
                                                             detail4 ->
    callback.apply(action, detail1, detail2, detail3, detail4)
}