package ksqlite

import ksqlite.types.Sqlite3CompleteResult
import ksqlite.types.Sqlite3Result
import ksqlite.types.sqlite3Results

///////////////////////////////////////////////////////////////////////////
// Result
///////////////////////////////////////////////////////////////////////////

/**
 * [Sqlite3Result]s associated by their integer code.
 */
private val Sqlite3ResultMap = sqlite3Results().associateBy(Sqlite3Result::code)

/**
 * Converts [resultCode] to [Sqlite3Result].
 */
internal fun convertResult(resultCode: Int): Sqlite3Result {
    return checkNotNull(Sqlite3ResultMap[resultCode]) {
        "Unknown sqlite3 result code $resultCode"
    }
}

///////////////////////////////////////////////////////////////////////////
// Complete
///////////////////////////////////////////////////////////////////////////

/**
 * Converts [resultCode] to [Sqlite3CompleteResult].
 */
internal fun convertCompleteResult(resultCode: Int): Sqlite3CompleteResult = when (resultCode) {
    0 -> Sqlite3CompleteResult.Incomplete
    1 -> Sqlite3CompleteResult.Complete
    else -> Sqlite3CompleteResult.Failure(
        checkNotNull(convertResult(resultCode) as? Sqlite3Result.Failure)
    )
}