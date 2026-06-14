package ksqlite.types.internal

import ksqlite.types.SqliteDataType

/**
 * Returns all [SqliteDataType]s.
 */
internal fun sqliteDataTypes(): Set<SqliteDataType> = setOf(
    SqliteDataType.INTEGER,
    SqliteDataType.FLOAT,
    SqliteDataType.TEXT,
    SqliteDataType.BLOB,
    SqliteDataType.NULL
)

/**
 * [ksqlite.types.SqliteDataType]s associated by their integer code.
 */
private val SqliteDataTypeMap = sqliteDataTypes().associateBy(SqliteDataType::code)

/**
 * Converts [type] to [ksqlite.types.SqliteDataType].
 */
public fun convertDataType(type: Int): SqliteDataType {
    return checkNotNull(SqliteDataTypeMap[type]) {
        "Unknown sqlite data type $type"
    }
}