package ksqlite.kapi

import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.sqlite3_compileoption_get
import ksqlite.capi.sqlite3_complete
import ksqlite.capi.sqlite3_initialize
import ksqlite.capi.sqlite3_shutdown
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.config.ConfigurationScopeImpl
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.sqliteResultThrow
import ksqlite.types.SqliteCompleteResult

///////////////////////////////////////////////////////////////////////////
// Instance
///////////////////////////////////////////////////////////////////////////

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

///////////////////////////////////////////////////////////////////////////
// Compilation
///////////////////////////////////////////////////////////////////////////

/**
 * Lists all the options defined at compile-time.
 */
@Suppress("FoldInitializerAndIfToElvis")
private fun sqliteListCompileOptions(): List<String> {
    var option: String? = sqlite3_compileoption_get(0)

    if (option == null) {
        return emptyList()
    }

    val options = mutableListOf(option)
    var index = 1

    do {
        option = sqlite3_compileoption_get(index++)
        option?.let(options::add)
    } while (option != null)

    return options.toList()
}

/**
 * Options defined at compile-time.
 */
internal val SqliteCompileOptions: List<String> by lazy(::sqliteListCompileOptions)

///////////////////////////////////////////////////////////////////////////
// Static
///////////////////////////////////////////////////////////////////////////

/**
 * Returns whether [sql] is a complete SQL statement.
 */
internal fun sqliteIsComplete(sql: String): Boolean = when (val result = sqlite3_complete(sql)) {
    SqliteCompleteResult.Complete -> true
    SqliteCompleteResult.Incomplete -> false
    is SqliteCompleteResult.Failure -> sqliteResultThrow(result.result, null)
}