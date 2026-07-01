package ksqlite.types.internal

import ksqlite.types.SqliteDataType

/**
 * [ksqlite.types.SqliteDataType]s associated by their integer code.
 */
private val SqliteDataTypeMap = SqliteDataType.entries.associateBy(SqliteDataType::code)

/**
 * Converts [type] to [ksqlite.types.SqliteDataType].
 */
public fun convertDataType(type: Int): SqliteDataType =
    checkNotNull(SqliteDataTypeMap[type]) { "Unknown SQLite data type $type" }