package ksqlite.types.internal

import ksqlite.types.SqliteTraceEventCode
import ksqlite.types.SqliteTransactionState

/**
 * Returns all constants [SqliteTraceEventCode.Constant]s.
 */
private fun sqliteTraceConstants(): Set<SqliteTraceEventCode.Constant> = setOf(
    SqliteTraceEventCode.STMT,
    SqliteTraceEventCode.PROFILE,
    SqliteTraceEventCode.ROW,
    SqliteTraceEventCode.CLOSE
)

/**
 * [ksqlite.types.SqliteTraceEventCode.Constant]s associated by their code value.
 */
private val TraceConstantMap = sqliteTraceConstants().associateBy(SqliteTraceEventCode::value)

/**
 * Converts [code] to [SqliteTransactionState].
 */
public fun convertTraceCode(code: Int): SqliteTraceEventCode.Constant =
    checkNotNull(TraceConstantMap[code]) { "Unknown SQLite trace code $code" }