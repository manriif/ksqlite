@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.kapi

import co.touchlab.stately.collections.ConcurrentMutableMap
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.sqlite3_initialize
import ksqlite.capi.sqlite3_open_v2
import ksqlite.capi.sqlite3_shutdown
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.config.AnyTimeConfigurationScope
import ksqlite.kapi.config.ConfigurationScope
import ksqlite.kapi.connection.Connection
import ksqlite.kapi.connection.ConnectionImpl
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.types.SqliteOpenFlag
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal class SQLiteImpl(private val onClose: () -> Unit) : SQLite {

    private val autoExtensions = mutableSetOf<AutoExtension>()
    private val autoExtensionLock = Lock()

    private val connections = ConcurrentMutableMap<sqlite3, ConnectionImpl>()

    private val closed = AtomicBoolean(false)

    override fun configure(action: AnyTimeConfigurationScope.() -> Unit) {

    }

    override fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db,
        vfs: String?
    ): Connection {
        val extensions = autoExtensionLock.withLock(autoExtensions::toList)

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
    fun requireConnection(db: sqlite3): Connection = checkNotNull(connections[db]) {
        "No connection is associated with database connection handle $db"
    }

    override fun addAutoExtension(autoExtension: AutoExtension): Unit =
        autoExtensionLock.withLock { autoExtensions.add(autoExtension) }

    override fun removeAutoExtension(autoExtension: AutoExtension): Unit =
        autoExtensionLock.withLock { autoExtensions.remove(autoExtension) }

    override fun close() {
        if (closed.compareAndSet(expectedValue = false, newValue = true)) {
            autoExtensionLock.withLock(autoExtensions::clear)
            connections.clear()
            onClose()
        }
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
 * Clears [SQLiteInstance].
 */
private fun clearSQLiteInstance() = SQLiteInstanceLock.withLock {
    check(SQLiteInstance != null)
    sqliteResultCheck(sqlite3_shutdown())
    SQLiteInstance = null
}

/**
 * Creates and sets [SQLiteInstance].
 */
internal fun createSQLiteInstance(configure: (ConfigurationScope.() -> Unit)? = null): SQLite {
    return SQLiteInstanceLock.withLock {
        check(SQLiteInstance == null) {
            "Only a single instance of SQLite is allowed simultaneously, previous instance must " +
                    "be shutdown first"
        }

        configure?.invoke()
        sqliteResultCheck(sqlite3_initialize())

        SQLiteImpl(::clearSQLiteInstance).also { instance ->
            SQLiteInstance = instance
        }
    }
}

/**
 * Retrieves the [Connection] associated with [db].
 */
internal fun requireConnection(db: sqlite3): Connection =
    SQLiteInstanceLock.withLock { sqlite.requireConnection(db) }