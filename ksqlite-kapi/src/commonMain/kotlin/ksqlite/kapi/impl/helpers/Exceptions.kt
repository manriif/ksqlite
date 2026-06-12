package ksqlite.kapi.impl.helpers

import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.sqlite3_errstr
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.throwSQLiteException

/**
 * Throws an [SQLiteException] with [Sqlite3Result.NOMEM] if [value], obtained from an SQLite API
 * call, is `null`.
 */
@PublishedApi
internal fun <T> sqliteOutOfMemoryCheck(
    value: T?,
    lazyMessage: () -> String
): T {
    if (value == null) {
        throwSQLiteException(lazyMessage(), Sqlite3Result.NOMEM)
    }

    return value
}

/**
 * Throws an [SQLiteException].
 *
 * If [getMessagePrefix] is supplied, the computed message is prepended before the SQLite message
 * associated with the database connection [db].
 *
 * This function must be called immediately after the [result] is obtained and before any other
 * SQLite API call is made.
 */
private fun sqliteResultThrow(
    result: Sqlite3Result,
    db: sqlite3?,
    getMessagePrefix: (() -> String)? = null
) {
    if (result !is Sqlite3Result.Failure) {
        return
    }

    val dbErrorMessage = db?.let(::sqlite3_errmsg)
    val messagePrefix = getMessagePrefix?.invoke()

    val message = when {
        dbErrorMessage != null && messagePrefix != null -> "$messagePrefix: $dbErrorMessage"
        dbErrorMessage != null -> dbErrorMessage
        messagePrefix != null -> messagePrefix
        else -> sqlite3_errstr(result.code) ?: "SQLite failed with result code ${result.code}"
    }

    throwSQLiteException(message, result)
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
    if (result is Sqlite3Result.Failure) {
        sqliteResultThrow(result, getDb?.invoke(), getMessagePrefix)
    }
}

/**
 * Throws an [SQLiteException] if the [result] is an [Sqlite3Result.Failure].
 *
 * If [getMessagePrefix] is supplied, the computed message is prepended before the SQLite message
 * associated with `this` database connection.
 *
 * This function must be called immediately after the [result] is obtained and before any other
 * SQLite API call is made.
 */
@PublishedApi
internal fun sqlite3.resultCheck(
    result: Sqlite3Result,
    getMessagePrefix: (() -> String)? = null
) {
    if (result is Sqlite3Result.Failure) {
        sqliteResultThrow(result, this, getMessagePrefix)
    }
}

/**
 * Invokes and returns [block]'s result, catching [SQLiteException]. The caught exception is then
 * passed to [handleException]. Other exceptions are uncaught and are rethrown in-place.
 */
internal inline fun <T, R> T.runCatchingSQLiteException(
    handleException: (SQLiteException) -> R,
    block: T.() -> R
): R = try {
    block()
} catch (exception: SQLiteException) {
    handleException(exception)
}