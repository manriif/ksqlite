package ksqlite.kapi

import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.sqlite3_errstr
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3

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
) : Exception(message)

///////////////////////////////////////////////////////////////////////////
// Factories
///////////////////////////////////////////////////////////////////////////

/**
 * Throws an [SQLiteException] with [Sqlite3Result.NOMEM] if [value] obtained from an SQLite API
 * call is `null`.
 */
@PublishedApi
internal fun <T> sqliteOutOfMemoryCheck(
    value: T?,
    lazyMessage: () -> String
): T {
    if (value == null) {
        throw SQLiteException(Sqlite3Result.NOMEM, lazyMessage())
    }

    return value
}

/**
 * Throws an [SQLiteException] if the [result] is an [Sqlite3Result.Failure].
 *
 * If [getMessagePrefix] is supplied, the computed message is prepended before the SQLite message
 * associated with the database connection provided by [getDb].
 *
 * This function must be called immediately after the [result] is obtained and before any other
 * SQLite API call is made.
 */
@PublishedApi
internal fun sqliteResultCheck(
    result: Sqlite3Result,
    getDb: (() -> sqlite3)?,
    getMessagePrefix: (() -> String)? = null
) {
    if (result !is Sqlite3Result.Failure) {
        return
    }

    val db = getDb?.invoke()
    val dbErrorMessage = db?.let(::sqlite3_errmsg)
    val messagePrefix = getMessagePrefix?.invoke()

    val message = when {
        dbErrorMessage != null && messagePrefix != null -> "$messagePrefix: $dbErrorMessage"
        dbErrorMessage != null -> dbErrorMessage
        messagePrefix != null -> messagePrefix
        else -> sqlite3_errstr(result.code) ?: "SQLite failed with result code ${result.code}"
    }

    throw SQLiteException(result, message)
}