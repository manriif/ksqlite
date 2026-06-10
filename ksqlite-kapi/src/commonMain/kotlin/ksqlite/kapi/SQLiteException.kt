package ksqlite.kapi

import ksqlite.capi.types.Sqlite3Result

/**
 * Exception resulting from a call to an SQLite API that failed, returning a non-successful result
 * code.
 */
public class SQLiteException(
    /**
     * The result returned by the API call that failed.
     */
    public val result: Sqlite3Result.Failure,
    message: String
): Exception(message)

///////////////////////////////////////////////////////////////////////////
// Factories
///////////////////////////////////////////////////////////////////////////

/**
 * Throws an [SQLiteException] with [Sqlite3Result.NOMEM] if [value] obtained from an SQLite API
 * call is `null`.
 */
@PublishedApi
internal fun <T> checkOutOfMemory(value: T?, lazyMessage: () -> String): T {
    if (value == null) {
        throw SQLiteException(Sqlite3Result.NOMEM, lazyMessage())
    }

    return value
}