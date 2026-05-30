package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.callbacks.Sqlite3TraceCallback
import ksqlite.capi.types.Sqlite3SqlLogEvent
import ksqlite.capi.types.Sqlite3TraceCode
import ksqlite.capi.types.Sqlite3TraceEvent
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3TraceConstants
import ksqlite.capi.types.sqlite3_stmt

///////////////////////////////////////////////////////////////////////////
// SqlLog
///////////////////////////////////////////////////////////////////////////

/**
 * Dispatches [Sqlite3SqlLogEvent] to [callback].
 */
internal fun <AppData> dispatchSqlLogEvent(
    callback: Sqlite3ConfigSqlLogCallback<AppData>,
    appData: AppData,
    type: Int,
    db: sqlite3,
    name: String?
): Unit = callback.handle(
    appData = appData,
    db = db,
    event = when (type) {
        0 -> Sqlite3SqlLogEvent.DatabaseOpened(name!!)
        1 -> Sqlite3SqlLogEvent.StatementExecuted(name!!)
        2 -> Sqlite3SqlLogEvent.DatabaseClosed
        else -> error("Unknown sql log event type: $type")
    }
)

///////////////////////////////////////////////////////////////////////////
// Trace
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3TraceCode.Constant]s associated by their code value.
 */
private val TraceConstantMap = sqlite3TraceConstants().associateBy(Sqlite3TraceCode::value)

/**
 * Dispatches [Sqlite3TraceEvent] to [callback].
 */
internal fun <P, X, AppData> dispatchTraceEvent(
    callback: Sqlite3TraceCallback<AppData>,
    appData: AppData,
    code: Int,
    pPointer: P?,
    xPointer: X?,
    toDb: (P) -> sqlite3,
    toStatement: (P) -> sqlite3_stmt,
    toString: (X) -> String,
    toLong: (X) -> Long
): Int = callback.handle(
    appData = appData,
    event = when (TraceConstantMap[code]) {
        Sqlite3TraceCode.STMT -> Sqlite3TraceEvent.Stmt(
            stmt = toStatement(pPointer!!),
            sql = toString(xPointer!!)
        )

        Sqlite3TraceCode.PROFILE -> Sqlite3TraceEvent.Profile(
            stmt = toStatement(pPointer!!),
            nanos = toLong(xPointer!!)
        )

        Sqlite3TraceCode.ROW -> Sqlite3TraceEvent.Row(toStatement(pPointer!!))
        Sqlite3TraceCode.CLOSE -> Sqlite3TraceEvent.Close(toDb(pPointer!!))
        null -> error("Unknown trace event type: $code")
    }
)