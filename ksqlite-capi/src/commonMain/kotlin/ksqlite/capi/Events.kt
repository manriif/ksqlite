package ksqlite.capi

import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.types.SqliteTraceEvent
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.types.SqliteSqlLogEvent
import ksqlite.types.SqliteTraceEventCode
import ksqlite.types.internal.convertTraceCode

///////////////////////////////////////////////////////////////////////////
// SqlLog
///////////////////////////////////////////////////////////////////////////

/**
 * Dispatches [SqliteSqlLogEvent] to [callback].
 */
internal fun <AppData> dispatchSqlLogEvent(
    callback: SqliteConfigSqlLogCallback<AppData>,
    appData: AppData,
    type: Int,
    db: sqlite3,
    name: String?
): Unit = callback.apply(
    appData = appData,
    db = db,
    event = when (type) {
        0 -> SqliteSqlLogEvent.DatabaseOpened(name!!)
        1 -> SqliteSqlLogEvent.StatementExecuted(name!!)
        2 -> SqliteSqlLogEvent.DatabaseClosed
        else -> error("Unknown sql log event type: $type")
    }
)

///////////////////////////////////////////////////////////////////////////
// Trace
///////////////////////////////////////////////////////////////////////////

/**
 * Dispatches [SqliteTraceEvent] to [callback].
 */
internal fun <P, X, AppData> dispatchTraceEvent(
    callback: SqliteTraceCallback<AppData>,
    appData: AppData,
    code: Int,
    pPointer: P?,
    xPointer: X?,
    toDb: (P) -> sqlite3,
    toStatement: (P) -> sqlite3_stmt,
    toString: (X) -> String,
    toLong: (X) -> Long
): Int = callback.apply(
    appData = appData,
    event = when (convertTraceCode(code)) {
        SqliteTraceEventCode.STMT -> SqliteTraceEvent.Stmt(
            stmt = toStatement(pPointer!!),
            sql = toString(xPointer!!)
        )

        SqliteTraceEventCode.PROFILE -> SqliteTraceEvent.Profile(
            stmt = toStatement(pPointer!!),
            nanos = toLong(xPointer!!)
        )

        SqliteTraceEventCode.ROW -> SqliteTraceEvent.Row(toStatement(pPointer!!))
        SqliteTraceEventCode.CLOSE -> SqliteTraceEvent.Close(toDb(pPointer!!))
    }
)