package ksqlite.capi

import ksqlite.capi.types.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.types.Sqlite3SqlLogEvent
import ksqlite.capi.types.Sqlite3TraceCallback
import ksqlite.capi.types.Sqlite3TraceCode
import ksqlite.capi.types.Sqlite3TraceEvent
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3TraceConstants
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_stmt

// TODO move this file to ksqlite.capi.handler

///////////////////////////////////////////////////////////////////////////
// SqlLog
///////////////////////////////////////////////////////////////////////////

/**
 * Dispatches [Sqlite3SqlLogEvent] to [callback].
 */
internal fun dispatchSqlLogEvent(
    callback: Sqlite3ConfigSqlLogCallback,
    userData: sqlite3_mutable_pointer?,
    type: Int,
    db: sqlite3,
    name: String?
): Unit = callback(
    userData,
    db,
    when (type) {
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
internal fun <Pointer> dispatchTraceEvent(
    callback: Sqlite3TraceCallback,
    userData: sqlite3_mutable_pointer?,
    code: Int,
    pointer1: Pointer?,
    pointer2: Pointer?,
    toDb: (Pointer) -> sqlite3,
    toStatement: (Pointer) -> sqlite3_stmt,
    toString: (Pointer) -> String,
    toLong: (Pointer) -> Long
): Int = callback(
    userData,
    when (TraceConstantMap[code]) {
        Sqlite3TraceCode.STMT -> Sqlite3TraceEvent.Stmt(
            stmt = toStatement(pointer1!!),
            sql = toString(pointer2!!)
        )

        Sqlite3TraceCode.PROFILE -> Sqlite3TraceEvent.Profile(
            stmt = toStatement(pointer1!!),
            nanos = toLong(pointer2!!)
        )

        Sqlite3TraceCode.ROW -> Sqlite3TraceEvent.Row(toStatement(pointer1!!))
        Sqlite3TraceCode.CLOSE -> Sqlite3TraceEvent.Close(toDb(pointer1!!))
        null -> error("Unknown trace event type: $code")
    }
)