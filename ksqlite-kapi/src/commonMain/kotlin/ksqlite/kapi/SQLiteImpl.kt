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
import ksqlite.capi.sqlite3_status64
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_stmt
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.config.AnyTimeConfigurationImpl
import ksqlite.kapi.database.AutoExtension
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.database.DatabaseConnectionImpl
import ksqlite.kapi.helpers.AtomicClosableScope
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParam
import ksqlite.kapi.helpers.usingParams
import ksqlite.kapi.statement.PreparedStatement
import ksqlite.kapi.value.StatusValue
import ksqlite.kapi.value.StatusValueImpl
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteStatusOption

internal class SQLiteImpl(private val shutdown: () -> Unit) :
    SQLite,
    AtomicClosableScope() {

    private val listener = Listener()
    private val autoExtensions = ConcurrentMutableSet<AutoExtension>()
    private val connections = ConcurrentMutableMap<sqlite3, DatabaseConnection>()
    private val statements = ConcurrentMutableMap<sqlite3_stmt, PreparedStatement>()

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

    /**
     * Retrieves the statement associated with [stmt].
     */
    fun requireStatement(stmt: sqlite3_stmt): PreparedStatement = notClosed {
        checkNotNull(statements[stmt]) {
            "No statement is associated with statement handle $stmt"
        }
    }

    override fun addAutoExtension(autoExtension: AutoExtension): Unit =
        notClosed { autoExtensions.add(autoExtension) }

    override fun removeAutoExtension(autoExtension: AutoExtension): Unit =
        notClosed { autoExtensions.remove(autoExtension) }

    override fun clearAutoExtensions(): Unit =
        notClosed { autoExtensions.clear() }

    override fun getMemoryStatus(reset: Boolean): StatusValue = StatusValueImpl(
        current = sqlite3_memory_used(),
        highwater = sqlite3_memory_highwater(if (reset) 1 else 0)
    )

    override fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db,
        vfs: String?
    ): DatabaseConnection = notClosed {
        val extensions = autoExtensions.block { it.toSet() }

        val db = usingParam(sqlite3.OutputParam()) { outDb ->
            sqliteResultCheck(sqlite3_open_v2(fileName, outDb, flags, vfs))
        }

        val connection = DatabaseConnectionImpl(db, listener)

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

    override fun getStatus(
        option: SqliteStatusOption,
        reset: Boolean
    ): StatusValue = notClosed {
        usingParams(
            param1 = Int64OutputParam(-1),
            param2 = Int64OutputParam(-1),
            transform = ::StatusValueImpl
        ) { outCur, outHighwater ->
            sqliteResultCheck(
                sqlite3_status64(
                    option = option,
                    outCurrent = outCur,
                    outHighwater = outHighwater,
                    resetFlag = if (reset) 1 else 0,
                )
            )
        }
    }

    override fun onClose() {
        config.close()
        autoExtensions.clear()
        connections.clear()
        shutdown()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////

    private inner class Listener : DatabaseConnectionImpl.Listener {

        override fun onStatementCreated(
            stmt: sqlite3_stmt,
            statement: PreparedStatement
        ) {
            check(statements.put(stmt, statement) == null) {
                "A statement is already associated with the statement handle $stmt"
            }
        }

        override fun onStatementClosed(stmt: sqlite3_stmt) {
            check(statements.remove(stmt) != null) {
                "Expected a statement to be registered with the statement handle $stmt"
            }
        }

        override fun onConnectionClosed(db: sqlite3) {
            check(connections.remove(db) != null) {
                "Expected a connection to be registered with the database connection handle $db"
            }
        }
    }
}