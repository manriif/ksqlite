package ksqlite.types.internal

import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode

/**
 * Returns all [SqliteVtabConstraintOperatorCode]s except custom.
 */
private fun sqliteVtabConstraintOperatorCodes(): Set<SqliteVtabConstraintOperatorCode> = setOf(
    SqliteVtabConstraintOperatorCode.EQ,
    SqliteVtabConstraintOperatorCode.GT,
    SqliteVtabConstraintOperatorCode.LE,
    SqliteVtabConstraintOperatorCode.LT,
    SqliteVtabConstraintOperatorCode.GE,
    SqliteVtabConstraintOperatorCode.MATCH,
    SqliteVtabConstraintOperatorCode.LIKE,
    SqliteVtabConstraintOperatorCode.GLOB,
    SqliteVtabConstraintOperatorCode.REGEXP,
    SqliteVtabConstraintOperatorCode.NE,
    SqliteVtabConstraintOperatorCode.ISNOT,
    SqliteVtabConstraintOperatorCode.ISNOTNULL,
    SqliteVtabConstraintOperatorCode.ISNULL,
    SqliteVtabConstraintOperatorCode.IS,
    SqliteVtabConstraintOperatorCode.LIMIT,
    SqliteVtabConstraintOperatorCode.OFFSET,
    SqliteVtabConstraintOperatorCode.FUNCTION
)

/**
 * [SqliteVtabConstraintOperatorCode]s associated by their integer code.
 */
private val SqliteVtabConstraintOperatorCodeMap =
    sqliteVtabConstraintOperatorCodes().associateBy(SqliteVtabConstraintOperatorCode::code)

/**
 * Converts [code] to [SqliteVtabConstraintOperatorCode].
 */
public fun convertVtabConstraintOperatorCode(code: Int): SqliteVtabConstraintOperatorCode =
    SqliteVtabConstraintOperatorCodeMap[code] ?: SqliteVtabConstraintOperatorCode.Custom(code)