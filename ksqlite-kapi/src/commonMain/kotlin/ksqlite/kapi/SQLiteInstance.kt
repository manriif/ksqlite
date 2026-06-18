package ksqlite.kapi

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.sqlite3_initialize
import ksqlite.capi.sqlite3_shutdown
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.config.ConfigurationScopeImpl
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.helpers.sqliteResultCheck

private var SQLiteInstance: SQLiteImpl? = null
private var SQLiteInstanceLock = Lock()

private val sqlite: SQLiteImpl
    get() = checkNotNull(SQLiteInstance) { "No SQLite instance exists or it was closed" }

/**
 * Shutdowns SQLite and clears [SQLiteInstance].
 */
private fun sqliteShutdown() = SQLiteInstanceLock.withLock {
    check(SQLiteInstance != null)
    sqliteResultCheck(sqlite3_shutdown())
    SQLiteInstance = null
}

/**
 * Initializes SQLite, sets and returns [SQLiteInstance].
 */
internal fun sqliteInitialize(configure: (ConfigurationScope.() -> Unit)? = null): SQLite {
    return SQLiteInstanceLock.withLock {
        check(SQLiteInstance == null) {
            "Only a single instance of SQLite is allowed simultaneously, previous instance must " +
                    "be shutdown first"
        }

        configure?.let { ConfigurationScopeImpl().use(it) }
        sqliteResultCheck(sqlite3_initialize())

        SQLiteImpl(::sqliteShutdown).also { instance ->
            SQLiteInstance = instance
        }
    }
}

/**
 * Retrieves the [DatabaseConnection] associated with [db].
 */
internal fun sqliteRequireConnection(db: sqlite3): DatabaseConnection =
    SQLiteInstanceLock.withLock { sqlite.requireConnection(db) }