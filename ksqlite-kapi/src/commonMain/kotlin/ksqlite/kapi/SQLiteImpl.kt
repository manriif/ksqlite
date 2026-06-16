package ksqlite.kapi

import co.touchlab.stately.collections.ConcurrentMutableMap
import co.touchlab.stately.collections.ConcurrentMutableSet
import ksqlite.capi.sqlite3_open_v2
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.config.AnyTimeConfigurationImpl
import ksqlite.kapi.database.AutoExtension
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.database.DatabaseConnectionImpl
import ksqlite.kapi.helpers.AtomicClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.types.SqliteOpenFlag

internal class SQLiteImpl(private val shutdown: () -> Unit) :
    SQLite,
    AtomicClosableScope() {

    private val autoExtensions = ConcurrentMutableSet<AutoExtension>()
    private val connections = ConcurrentMutableMap<sqlite3, DatabaseConnectionImpl>()

    override val config = AnyTimeConfigurationImpl()

    override fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db,
        vfs: String?
    ): DatabaseConnection = notClosed {
        val extensions = autoExtensions.block { it.toSet() }

        val db = usingParam(SqliteOutputParam()) { outDb ->
            sqliteResultCheck(sqlite3_open_v2(fileName, outDb, flags, vfs))
        }

        val connection = DatabaseConnectionImpl(db) {
            connections.remove(db)
        }

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
    fun requireConnection(db: sqlite3): DatabaseConnection = notClosed {
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
        config.close()
        autoExtensions.clear()
        connections.clear()
        shutdown()
    }
}