/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.callbacks.SqliteUpdateHookCallback
import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.types.SqliteTraceEvent
import ksqlite.kapi.helpers.ksqliteLog
import ksqlite.kapi.helpers.runCatchingSQLiteException
import ksqlite.kapi.sqliteRequireConnection
import ksqlite.kapi.sqliteRequireStatement
import ksqlite.types.SqliteResultCode

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
    callback.apply(
        action = action,
        detail1 = detail1,
        detail2 = detail2,
        detail3 = detail3,
        detail4 = detail4
    )
}

/**
 * Invokes [Trace.apply].
 */
internal val TraceCallback = SqliteTraceCallback { callback: Trace, event ->
    callback.apply(
        when (event) {
            is SqliteTraceEvent.Row -> TraceEvent.Row(sqliteRequireStatement(event.stmt))

            is SqliteTraceEvent.Stmt -> TraceEvent.Stmt(
                statement = sqliteRequireStatement(event.stmt),
                sql = event.sql
            )

            is SqliteTraceEvent.Profile -> TraceEvent.Profile(
                statement = sqliteRequireStatement(event.stmt),
                nanos = event.nanos
            )

            is SqliteTraceEvent.Close -> TraceEvent.Close(sqliteRequireConnection(event.db))
        }
    )

    0
}

/**
 * Invokes [UpdateHook.apply].
 */
internal val UpdateHookCallback = SqliteUpdateHookCallback { callback: UpdateHook,
                                                             action,
                                                             databaseName,
                                                             tableName,
                                                             rowid ->
    callback.apply(
        action = action,
        databaseName = databaseName,
        tableName = tableName,
        rowid = rowid
    )
}

/**
 * Invokes [WriteAheadLogHook.apply].
 */
internal val WriteAheadLogHookCallback = SqliteWalHookCallback { callback: WriteAheadLogHook,
                                                                 db,
                                                                 databaseName,
                                                                 pageCount ->
    callback.runCatchingSQLiteException({ error ->
        ksqliteLog(error, error.result)
        error.result
    }) {
        callback.apply(
            connection = sqliteRequireConnection(db),
            databaseName = databaseName,
            pageCount = pageCount
        )

        SqliteResultCode.OK
    }
}