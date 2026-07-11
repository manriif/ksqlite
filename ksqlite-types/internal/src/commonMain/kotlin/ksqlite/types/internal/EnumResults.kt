package ksqlite.types.internal

import ksqlite.types.SqliteCompleteResult
import ksqlite.types.SqliteDbReadonlyResult
import ksqlite.types.SqliteResultCode

/**
 * Converts [value] to [SqliteCompleteResult].
 */
public fun convertCompleteResult(value: Int): SqliteCompleteResult = when (value) {
    0 -> Incomplete
    1 -> Complete
    else -> SqliteCompleteResult
        .Failure(checkNotNull(convertResultCode(value) as? SqliteResultCode.Failure))
}

/**
 * Converts [value] to [SqliteDbReadonlyResult].
 */
public fun convertDbReadonlyResult(value: Int): SqliteDbReadonlyResult = when (value) {
    0 -> READWRITE
    1 -> READONLY
    -1 -> UNKNOWN_DATABASE
    else -> error("Unexpected result from sqlite3_db_readonly(): $value")
}