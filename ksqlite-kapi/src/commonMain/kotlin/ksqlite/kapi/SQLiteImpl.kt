/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.kapi

import co.touchlab.stately.collections.ConcurrentMutableMap
import co.touchlab.stately.collections.ConcurrentMutableSet
import co.touchlab.stately.concurrency.Lock
import co.touchlab.stately.concurrency.close
import co.touchlab.stately.concurrency.withLock
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_hard_heap_limit64
import ksqlite.capi.sqlite3_memory_highwater
import ksqlite.capi.sqlite3_memory_used
import ksqlite.capi.sqlite3_open_v2
import ksqlite.capi.sqlite3_randomness
import ksqlite.capi.sqlite3_release_memory
import ksqlite.capi.sqlite3_soft_heap_limit64
import ksqlite.capi.sqlite3_status64
import ksqlite.capi.sqlite3_stmt
import ksqlite.internal.runtime.closeable.AtomicCloseableScope
import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.cipher.CipherManagerImpl
import ksqlite.kapi.config.AnyTimeConfigurationImpl
import ksqlite.kapi.connection.AutoExtension
import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.kapi.connection.DatabaseConnectionImpl
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.sqliteResultThrow
import ksqlite.kapi.helpers.usingParams
import ksqlite.kapi.statement.PreparedStatement
import ksqlite.kapi.statement.PreparedStatementImpl
import ksqlite.kapi.value.Status
import ksqlite.kapi.value.StatusImpl
import ksqlite.kapi.vfs.VirtualFileSystemManagerImpl
import ksqlite.types.SqliteOpenFlag
import ksqlite.types.SqliteStatusOption
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal class SQLiteImpl(private val shutdown: () -> Unit) :
    SQLite,
    AtomicCloseableScope() {

    private val listener = Listener()
    private val autoExtensions = ConcurrentMutableSet<AutoExtension>()
    private val statements = ConcurrentMutableMap<sqlite3_stmt, PreparedStatement>()

    private val connections = mutableMapOf<sqlite3, DatabaseConnection>()
    private val connectionsLock = Lock()

    override val config = AnyTimeConfigurationImpl(this)
    override val ciphers = CipherManagerImpl(this)
    override val virtualFileSystems = VirtualFileSystemManagerImpl(this)

    override var hardHeapLimit: Long
        get() = notClosed { sqlite3_hard_heap_limit64(-1) }
        set(value) = notClosed { setHeapLimit(value, ::sqlite3_hard_heap_limit64) }

    override val memoryUsed: Long
        get() = notClosed { sqlite3_memory_used() }

    override val memoryHighwater: Long
        get() = notClosed { sqlite3_memory_highwater(0) }

    override var softHeapLimit: Long
        get() = notClosed { sqlite3_soft_heap_limit64(-1) }
        set(value) = notClosed { setHeapLimit(value, ::sqlite3_soft_heap_limit64) }

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
    fun requireConnection(
        db: sqlite3,
        create: Boolean
    ): DatabaseConnection = notClosed {
        // Reentrancy is important here because if a database is being closed then a hook invoked
        // from the closing thread should access the current connection instance. Other thread must
        // by opposite wait because db may have been attributed the same pointer as the connection
        // being closed
        connectionsLock.withLock {
            connections.getOrPut(db) {
                check(create) { "No connection is associated with database connection handle $db" }
                DatabaseConnectionImpl(db, listener)
            }
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

    override fun getMemoryStatus(reset: Boolean): Status = notClosed {
        StatusImpl(
            current = sqlite3_memory_used(),
            highwater = sqlite3_memory_highwater(if (reset) 1 else 0)
        )
    }

    override fun open(
        fileName: String,
        flags: SqliteOpenFlag.Db,
        vfs: String?
    ): DatabaseConnection = notClosed {
        val extensions = autoExtensions.block { it.toSet() }
        val outDb = sqlite3.OutputParam()

        val db = when (val openResult = sqlite3_open_v2(fileName, outDb, flags, vfs)) {
            OK -> outDb.value!!
            is Failure -> sqliteResultThrow(openResult, outDb.value)
            else -> error("Unexpected result from sqlite3_open_v2: $openResult")
        }

        val connection = requireConnection(db, true)

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

        return connection
    }

    override fun generateRandomBytes(output: Buffer, size: Int) =
        notClosed { sqlite3_randomness(size, output.buffer) }

    override fun releaseMemory(size: Int): Int =
        notClosed { sqlite3_release_memory(size) }

    override fun getStatus(
        option: SqliteStatusOption,
        reset: Boolean
    ): Status = notClosed {
        usingParams(
            param1 = Int64OutputParam(-1),
            param2 = Int64OutputParam(-1),
            transform = ::StatusImpl
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
        ciphers.close()
        autoExtensions.clear()
        statements.clear()

        connectionsLock.withLock {
            connections.clear()
        }

        connectionsLock.close()
        shutdown()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////

    private inner class Listener : DatabaseConnectionImpl.Listener {

        override fun onStatementCreated(statement: PreparedStatementImpl) {
            check(statements.put(statement.stmt, statement) == null) {
                "A statement is already associated with the statement handle ${statement.stmt}"
            }
        }

        override fun <R> onFinalizingStatement(
            statement: PreparedStatementImpl,
            block: () -> R
        ): R {
            check(statements.remove(statement.stmt) == statement) {
                "Expected a statement to be registered with the statement handle ${statement.stmt}"
            }

            return block()
        }

        override fun <R> onClosingConnection(
            connection: DatabaseConnectionImpl,
            block: () -> R
        ): R = connectionsLock.withLock {
            val currentConnection = checkNotNull(connections[connection.db]) {
                "Expected a connection to be registered with the database connection handle " +
                        connection.db
            }

            check(currentConnection === connection)

            // Hook may be invoked in block and try to access the connection so thats why it has
            // not been removed in previous lookup
            val result = block()

            check(connections.remove(currentConnection.db) === currentConnection)
            result
        }
    }
}