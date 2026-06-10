package ksqlite.kapi

import ksqlite.capi.types.Sqlite3Result

/**
 * Exception resulting from a call to an SQLite API that failed, returning a non-successful result
 * code.
 *
 * This exception can also be thrown in an SQLite callback, the [result] and the error [message]
 * are then forwarded to SQLite.
 */
public open class SQLiteException(
    /**
     * The result returned by the API call that failed.
     */
    public val result: Sqlite3Result.Failure,
    override val message: String
) : Exception(message)

///////////////////////////////////////////////////////////////////////////
// Factories
///////////////////////////////////////////////////////////////////////////

/**
 * Throws an [SQLiteException] with supplied [message] and [result] which is default to
 * [Sqlite3Result.ERROR].
 */
public fun throwSQLiteException(
    message: String,
    result: Sqlite3Result.Failure = Sqlite3Result.ERROR
): Nothing {
    throw SQLiteException(result, message)
}