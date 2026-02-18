package ksqlite

import ksqlite.types.Sqlite3Result

/**
 * Returns an [ksqlite.types.Sqlite3Result] from [resultCode].
 */
internal fun typeSafeResult(resultCode: Int): Sqlite3Result = when (resultCode) {
    Sqlite3Result.OK.raw -> Sqlite3Result.OK
    Sqlite3Result.ROW.raw -> Sqlite3Result.ROW
    Sqlite3Result.DONE.raw -> Sqlite3Result.DONE
    else -> Sqlite3Result.Error(resultCode)
}