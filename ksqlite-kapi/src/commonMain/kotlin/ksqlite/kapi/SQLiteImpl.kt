package ksqlite.kapi

import co.touchlab.stately.collections.ConcurrentMutableMap
import co.touchlab.stately.collections.ConcurrentMutableSet
import ksqlite.capi.sqlite3_hard_heap_limit64
import ksqlite.capi.sqlite3_memory_highwater
import ksqlite.capi.sqlite3_memory_used
import ksqlite.capi.sqlite3_open_v2
import ksqlite.capi.sqlite3_randomness
import ksqlite.capi.sqlite3_release_memory
import ksqlite.capi.sqlite3_soft_heap_limit64
import ksqlite.capi.types.SqliteOutputParam
import ksqlite.capi.types.sqlite3
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.config.AnyTimeConfigurationImpl
import ksqlite.kapi.database.AutoExtension
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.database.DatabaseConnectionImpl
import ksqlite.kapi.helpers.AtomicClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.kapi.value.StatusValue
import ksqlite.types.SqliteOpenFlag

internal class SQLiteImpl(private val shutdown: () -> Unit) :
    SQLite,
    AtomicClosableScope() {

    private val autoExtensions = ConcurrentMutableSet<AutoExtension>()
    private val connections = ConcurrentMutableMap<sqlite3, DatabaseConnectionImpl>()

    override val config = AnyTimeConfigurationImpl()

    override var hardHeapLimit: Long
        get() = sqlite3_hard_heap_limit64(-1)
        set(value) = setHeapLimit(value, ::sqlite3_hard_heap_limit64)

    override val memoryUsed: Long
        get() = sqlite3_memory_used()

    override val memoryHighwater: Long
        get() = sqlite3_memory_highwater(0)

    override var softHeapLimit: Long
        get() = sqlite3_soft_heap_limit64(-1)
        set(value) = setHeapLimit(value, ::sqlite3_soft_heap_limit64)

    /**
     * Sets the heap limit using [block] and throws if it returns -1
     */
    private inline fun setHeapLimit(value: Long, block: (Long) -> Long) {
        if (block(value) == -1L) {
            throwSQLiteException("Failed to set the new heap limit")
        }
    }

    /**
     * Retrieves the connection associated with [db].
     */
    fun requireConnection(db: sqlite3): DatabaseConnection = notClosed {
        checkNotNull(connections[db]) {
            "No connection is associated with database connection handle $db"
        }
    }

    override fun addAutoExtension(autoExtension: AutoExtension): Unit =
        notClosed { autoExtensions.add(autoExtension) }

    override fun removeAutoExtension(autoExtension: AutoExtension): Unit =
        notClosed { autoExtensions.remove(autoExtension) }

    override fun clearAutoExtensions(): Unit =
        notClosed { autoExtensions.clear() }

    override fun getMemoryStatus(resetHighwater: Boolean): StatusValue = StatusValue(
        current = sqlite3_memory_used(),
        highwater = sqlite3_memory_highwater(if (resetHighwater) 1 else 0)
    )

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

    override fun generateRandomBytes(output: Buffer, size: Int) =
        notClosed { sqlite3_randomness(size, output.buffer) }

    override fun releaseMemory(size: Int): Int =
        notClosed { sqlite3_release_memory(size) }

    override fun onClose() {
        config.close()
        autoExtensions.clear()
        connections.clear()
        shutdown()
    }
}