package ksqlite.types.internal

import ksqlite.types.SqliteCompleteResult
import ksqlite.types.SqliteResultCode

/**
 * Converts [code] to [SqliteCompleteResult].
 */
public fun convertCompleteResult(code: Int): SqliteCompleteResult = when (code) {
    0 -> SqliteCompleteResult.Incomplete
    1 -> SqliteCompleteResult.Complete
    else -> SqliteCompleteResult
        .Failure(checkNotNull(convertResult(code) as? SqliteResultCode.Failure))
}