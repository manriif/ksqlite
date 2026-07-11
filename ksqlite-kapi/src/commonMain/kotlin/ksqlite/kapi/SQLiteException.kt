package ksqlite.kapi

import ksqlite.types.SqliteResultCode

/**
 * Exception resulting from a call to an SQLite API that failed, returning a non-successful result
 * code.
 *
 * This exception can also be thrown in an SQLite callback, the [result] and the error [message]
 * are then forwarded to SQLite.
 *
 * The helper function [throwSQLiteException] can be used to construct and throw an instance of
 * [SQLiteException].
 */
public open class SQLiteException(
    /**
     * The result returned by the API call that failed.
     */
    public val result: SqliteResultCode.Failure,
    override val message: String
) : RuntimeException(message)

///////////////////////////////////////////////////////////////////////////
// Factories
///////////////////////////////////////////////////////////////////////////

/**
 * Throws an [SQLiteException] with supplied [message] and [result] which is default to
 * [SqliteResultCode.ERROR].
 */
public fun throwSQLiteException(
    message: String,
    result: SqliteResultCode.Failure = SqliteResultCode.ERROR
): Nothing {
    throw SQLiteException(result, message)
}