package ksqlite.kapi.impl

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.types.Sqlite3ConfigOption
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.SQLite
import ksqlite.kapi.SQLiteConnection

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