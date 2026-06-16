package ksqlite.kapi

import co.touchlab.stately.collections.ConcurrentMutableMap
import co.touchlab.stately.collections.ConcurrentMutableSet
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.sqlite3_initialize
import ksqlite.capi.sqlite3_open_v2
import ksqlite.capi.sqlite3_shutdown
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.config.AnyTimeConfigurationScope
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.config.ConfigurationScopeImpl
import ksqlite.kapi.connection.AutoExtension
import ksqlite.kapi.connection.Connection
import ksqlite.kapi.connection.ConnectionImpl
import ksqlite.kapi.helpers.AtomicClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.types.SqliteOpenFlag

internal class SQLiteImpl(private val shutdown: () -> Unit) :
    SQLite,
    AtomicClosableScope() {

    private val autoExtensions = ConcurrentMutableSet<AutoExtension>()
    private val connections = ConcurrentMutableMap<sqlite3, ConnectionImpl>()

    override fun configure(action: AnyTimeConfigurationScope.() -> Unit) =
        notClosed { ConfigurationScopeImpl().use(action) }

    override fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db,
        vfs: String?
    ): Connection = notClosed {
        val extensions = autoExtensions.block { it.toSet() }

        val db = usingParam(SqliteOutputParam()) { outDb ->
            sqliteResultCheck(sqlite3_open_v2(fileName, outDb, flags, vfs))
        }

        val connection = ConnectionImpl(db)

        try {
            extensions.forEach { extension ->
                extension.apply(connection)
            }
        } catch (extensionException: SQLiteException) {
            var exception: Throwable = extensionException

            try {
                connection.close()
            } catch (closeException: Throwable) {
                closeException.addSuppressed(exception)
                exception = closeException
            }

            throw exception
        }

        check(connections.put(db, connection) == null) {
            "A connection is already associated with the database connection handle $db"
        }

        return connection
    }

    /**
     * Retrieves the connection associated with [db].
     */
    fun requireConnection(db: sqlite3): Connection = notClosed {
        checkNotNull(connections[db]) {
            "No connection is associated with database connection handle $db"
        }
    }

    override fun addAutoExtension(autoExtension: AutoExtension): Unit = notClosed {
        autoExtensions.add(autoExtension)
    }

    override fun removeAutoExtension(autoExtension: AutoExtension): Unit = notClosed {
        autoExtensions.remove(autoExtension)
    }

    override fun onClose() {
        autoExtensions.clear()
        connections.clear()
        shutdown()
    }
}

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
 * Retrieves the [Connection] associated with [db].
 */
internal fun requireConnection(db: sqlite3): Connection =
    SQLiteInstanceLock.withLock { sqlite.requireConnection(db) }