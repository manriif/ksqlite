package ksqlite.types.internal

import ksqlite.types.SqliteTraceCode
import ksqlite.types.SqliteTransactionState

/**
 * Returns all constants [SqliteTraceCode.Constant]s.
 */
private fun sqliteTraceConstants(): Set<SqliteTraceCode.Constant> = setOf(
    SqliteTraceCode.STMT,
    SqliteTraceCode.PROFILE,
    SqliteTraceCode.ROW,
    SqliteTraceCode.CLOSE
)

/**
 * [ksqlite.types.SqliteTraceCode.Constant]s associated by their code value.
 */
private val TraceConstantMap = sqliteTraceConstants().associateBy(SqliteTraceCode::code)

/**
 * Converts [code] to [SqliteTransactionState].
 */
public fun convertTraceCode(code: Int): SqliteTraceCode.Constant {
    return checkNotNull(TraceConstantMap[code]) {
        "Unknown sqlite trace code $code"
    }
}