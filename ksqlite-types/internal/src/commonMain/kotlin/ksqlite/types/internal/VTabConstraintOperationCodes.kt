package ksqlite.types.internal

import ksqlite.types.vtab.SqliteVTabConstraintOperatorCode

/**
 * Returns all [SqliteVTabConstraintOperatorCode]s except custom.
 */
private fun sqliteVTabConstraintOperatorCodes(): Set<SqliteVTabConstraintOperatorCode> = setOf(
    SqliteVTabConstraintOperatorCode.EQ,
    SqliteVTabConstraintOperatorCode.GT,
    SqliteVTabConstraintOperatorCode.LE,
    SqliteVTabConstraintOperatorCode.LT,
    SqliteVTabConstraintOperatorCode.GE,
    SqliteVTabConstraintOperatorCode.MATCH,
    SqliteVTabConstraintOperatorCode.LIKE,
    SqliteVTabConstraintOperatorCode.GLOB,
    SqliteVTabConstraintOperatorCode.REGEXP,
    SqliteVTabConstraintOperatorCode.NE,
    SqliteVTabConstraintOperatorCode.ISNOT,
    SqliteVTabConstraintOperatorCode.ISNOTNULL,
    SqliteVTabConstraintOperatorCode.ISNULL,
    SqliteVTabConstraintOperatorCode.IS,
    SqliteVTabConstraintOperatorCode.LIMIT,
    SqliteVTabConstraintOperatorCode.OFFSET,
    SqliteVTabConstraintOperatorCode.FUNCTION
)

/**
 * [SqliteVTabConstraintOperatorCode]s associated by their integer code.
 */
private val SqliteVTabConstraintOperatorCodeMap =
    sqliteVTabConstraintOperatorCodes().associateBy(SqliteVTabConstraintOperatorCode::code)

/**
 * Converts [code] to [SqliteVTabConstraintOperatorCode].
 */
public fun convertVTabConstraintOperatorCode(code: Int): SqliteVTabConstraintOperatorCode {
    return SqliteVTabConstraintOperatorCodeMap[code]
        ?: SqliteVTabConstraintOperatorCode.Custom(code)
}