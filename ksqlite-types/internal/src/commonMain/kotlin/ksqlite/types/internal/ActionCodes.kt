package ksqlite.types.internal

import ksqlite.types.SqliteActionCode

/**
 * Returns all [SqliteActionCode]s.
 */
private fun sqliteActionCodes(): Set<SqliteActionCode> = setOf(
    SqliteActionCode.INDEX,
    SqliteActionCode.TABLE,
    SqliteActionCode.TEMP_INDEX,
    SqliteActionCode.TEMP_TABLE,
    SqliteActionCode.TEMP_TRIGGER,
    SqliteActionCode.TEMP_VIEW,
    SqliteActionCode.TRIGGER,
    SqliteActionCode.VIEW,
    SqliteActionCode.DELETE,
    SqliteActionCode.DROP_INDEX,
    SqliteActionCode.DROP_TABLE,
    SqliteActionCode.DROP_TEMP_INDEX,
    SqliteActionCode.DROP_TEMP_TABLE,
    SqliteActionCode.DROP_TEMP_TRIGGER,
    SqliteActionCode.DROP_TEMP_VIEW,
    SqliteActionCode.DROP_TRIGGER,
    SqliteActionCode.DROP_VIEW,
    SqliteActionCode.INSERT,
    SqliteActionCode.PRAGMA,
    SqliteActionCode.READ,
    SqliteActionCode.SELECT,
    SqliteActionCode.TRANSACTION,
    SqliteActionCode.UPDATE,
    SqliteActionCode.ATTACH,
    SqliteActionCode.DETACH,
    SqliteActionCode.ALTER_TABLE,
    SqliteActionCode.REINDEX,
    SqliteActionCode.ANALYZE,
    SqliteActionCode.VTABLE,
    SqliteActionCode.DROP_VTABLE,
    SqliteActionCode.FUNCTION,
    SqliteActionCode.SAVEPOINT,
    SqliteActionCode.COPY,
    SqliteActionCode.RECURSIVE
)

/**
 * [SqliteActionCode]s associated by their integer code.
 */
@PublishedApi
internal val SqliteActionCodeMap: Map<Int, SqliteActionCode> =
    sqliteActionCodes().associateBy(SqliteActionCode::code)

/**
 * Converts [code] to [SqliteActionCode].
 */
public inline fun <reified A : SqliteActionCode> convertActionCode(code: Int): A {
    val actionCode = checkNotNull(SqliteActionCodeMap[code]) {
        "Unknown sqlite3 action code $code"
    }

    check(actionCode is A) { "Unexpected action type $actionCode" }
    return actionCode
}