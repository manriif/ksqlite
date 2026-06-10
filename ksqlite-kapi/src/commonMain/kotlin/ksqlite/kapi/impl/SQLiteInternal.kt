package ksqlite.kapi.impl

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.sqlite3_errmsg
import ksqlite.capi.sqlite3_errstr
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.SQLite
import ksqlite.kapi.SQLiteConnection
import ksqlite.kapi.SQLiteException
import ksqlite.kapi.throwSQLiteException

///////////////////////////////////////////////////////////////////////////
// SQLite
///////////////////////////////////////////////////////////////////////////

private var SQLiteInstance: SQLiteImpl? = null
private var SQLiteInstanceLock = Lock()

private val sqlite: SQLiteImpl
    get() = checkNotNull(SQLiteInstance) { "No SQLite instance exists or it was closed" }

/**
 * Clears [SQLiteInstance].
 */
private fun clearSQLiteInstance() = SQLiteInstanceLock.withLock {
    check(SQLiteInstance != null)
    SQLiteInstance = null
}

/**
 * Creates and sets [SQLiteInstance].
 */
internal fun createSQLiteInstance(options: List<Sqlite3ConfigOption>): SQLite {
    return SQLiteInstanceLock.withLock {
        check(SQLiteInstance == null) {
            "Only a single instance of SQLite is allowed simultaneously, previous instance must be " +
                    "shutdown first"
        }

        SQLiteImpl(options.toList(), ::clearSQLiteInstance).also { instance ->
            SQLiteInstance = instance
        }
    }
}

/**
 * Retrieves the [SQLiteConnection] associated with [db].
 */
internal fun retrieveConnection(db: sqlite3): SQLiteConnection =
    SQLiteInstanceLock.withLock { sqlite.requireConnection(db) }

///////////////////////////////////////////////////////////////////////////
// Exceptions
///////////////////////////////////////////////////////////////////////////

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

    throwSQLiteException(message, result)
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