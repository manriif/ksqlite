package ksqlite.kapi.helpers

import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.sqlite3_errstr
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.throwSQLiteException
import ksqlite.types.SqliteResultCode

/**
 * Throws an [SQLiteException] with [SqliteResultCode.NOMEM] if [value], obtained from an SQLite API
 * call, is `null`.
 */
@PublishedApi
internal fun <T> sqliteOutOfMemoryCheck(
    value: T?,
    lazyMessage: () -> String
): T {
    if (value == null) {
        throwSQLiteException(lazyMessage(), SqliteResultCode.NOMEM)
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
internal fun sqliteResultThrow(
    result: SqliteResultCode.Failure,
    db: sqlite3?,
    getMessagePrefix: (() -> String)? = null
): Nothing {
    val errorMessage = when (result) {
        MISUSE -> sqlite3_errstr(result.code)
        else -> db?.let(::sqlite3_errmsg) ?: sqlite3_errstr(result.code)
    }

    val messagePrefix = getMessagePrefix?.invoke()

    val message = when {
        errorMessage != null && messagePrefix != null -> "$messagePrefix: $errorMessage"
        errorMessage != null -> errorMessage
        messagePrefix != null -> messagePrefix
        else -> "SQLite failed with result code ${result.code}"
    }

    throwSQLiteException(message, result)
}

/**
 * Throws an [SQLiteException] if the [result] is an [SqliteResultCode.Failure].
 *
 * If [getMessagePrefix] is supplied, the computed message is prepended before the SQLite message
 * associated with the database connection provided by [getDb].
 *
 * This function must be called immediately after the [result] is obtained and before any other
 * SQLite API call is made.
 */
@PublishedApi
internal fun sqliteResultCheck(
    result: SqliteResultCode,
    getDb: (() -> sqlite3)? = null,
    getMessagePrefix: (() -> String)? = null
) {
    if (result is SqliteResultCode.Failure) {
        sqliteResultThrow(result, getDb?.invoke(), getMessagePrefix)
    }
}

/**
 * Throws an [SQLiteException] if the [result] is an [SqliteResultCode.Failure].
 *
 * If [getMessagePrefix] is supplied, the computed message is prepended before the SQLite message
 * associated with `this` database connection.
 *
 * This function must be called immediately after the [result] is obtained and before any other
 * SQLite API call is made.
 */
@PublishedApi
internal fun sqlite3.resultCheck(
    result: SqliteResultCode,
    getMessagePrefix: (() -> String)? = null
) {
    if (result is SqliteResultCode.Failure) {
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