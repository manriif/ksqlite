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
package ksqlite.kapi.connection

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.runSqliteConnectionDataTest
import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.kapi.runSqliteWalFileTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests the core [DatabaseConnection] APIs.
 */
class DatabaseConnectionTest {

    @Test
    fun lifecycleWorks() = runSqliteConnectionTest { _, connection ->
        assertTrue(connection.isAutocommit)
        connection.close()
        // Closing again is a no-op
        connection.close()
    }

    @Test
    fun setBusyTimeoutWorks() = runSqliteConnectionTest { _, connection ->
        connection.setBusyTimeout(1_000)
        connection.setBusyTimeout(500.milliseconds)
        connection.setBusyTimeout(0)
    }

    @Test
    fun changesAndCountersWork() = runSqliteConnectionDataTest { _, connection ->
        connection.execute("INSERT INTO test(integer_t) VALUES (1), (2);")

        assertEquals(2, connection.changes)
        assertEquals(2, connection.totalChanges)
        assertTrue(connection.lastInsertRowid > 0)

        connection.lastInsertRowid = 42
        assertEquals(42, connection.lastInsertRowid)

        assertTrue(connection.isAutocommit)
        assertTrue(!connection.isInterrupted)

        connection.interrupt()
    }

    @Test
    fun executeWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER, name TEXT);")
        connection.execute("INSERT INTO fruits VALUES (1, 'Kiwi'), (2, 'Mango');")

        val actualFruits = mutableMapOf<Int, String>()

        connection.execute("SELECT id, name FROM fruits ORDER BY id;") { count, values, names ->
            assertEquals(2, count)
            assertEquals("id", names[0])
            assertEquals("name", names[1])

            val id = assertNotNull(values[0]).toInt()
            val name = assertNotNull(values[1])
            actualFruits += id to name
            false
        }

        assertEquals(mapOf(1 to "Kiwi", 2 to "Mango"), actualFruits)
    }

    @Test
    fun executeCanBeAborted() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        connection.execute("INSERT INTO fruits VALUES (1), (2), (3);")

        var callCount = 0

        assertFailsWith<SQLiteException> {
            connection.execute("SELECT id FROM fruits ORDER BY id;") { _, _, _ ->
                callCount++
                true
            }
        }

        assertEquals(1, callCount)
    }

    @Test
    fun executeFailsOnInvalidSql() = runSqliteConnectionTest { _, connection ->
        assertFailsWith<SQLiteException> {
            connection.execute("CREATE table fail;")
        }
    }

    @Test
    fun prepareWorks() = runSqliteConnectionTest { _, connection ->
        val statement = connection.prepare("SELECT 1;")
        statement.close()
    }

    @Test
    fun prepareFailsOnInvalidSql() = runSqliteConnectionTest { _, connection ->
        assertFailsWith<SQLiteException> {
            connection.prepare("this is not sql;")
        }
    }

    @Test
    fun limitWorks() = runSqliteConnectionTest { _, connection ->
        val defaultLimit = connection.getLimit(LENGTH)
        assertEquals(1_000_000_000, defaultLimit)

        connection.setLimit(LENGTH, 100_000)
        assertEquals(100_000, connection.getLimit(LENGTH))
    }

    @Test
    fun nameAndFileNameWork() = runSqliteConnectionTest { _, connection ->
        assertEquals("main", connection.getName(0))
        assertEquals("temp", connection.getName(1))
        assertNull(connection.getName(2))

        connection.config.setMainDatabaseName("primary")
        assertEquals("primary", connection.getName(0))
    }

    @Test
    fun isReadOnlyWorks() = runSqliteConnectionTest { _, connection ->
        assertTrue(!connection.isReadOnly("main"))

        assertFailsWith<SQLiteException> {
            connection.isReadOnly("does_not_exist")
        }
    }

    @Test
    fun releaseMemoryAndStatusWork() = runSqliteConnectionTest { _, connection ->
        connection.releaseMemory()

        val status = connection.getStatus(CACHE_USED)
        assertTrue(status.current >= 0)
        assertTrue(status.highwater >= 0)
    }

    @Test
    fun tableColumnMetadataWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE test(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT);")

        val metadata = connection.tableColumnMetadata(table = "test", column = "id")

        assertEquals("INTEGER", metadata.dataType)
        assertEquals("BINARY", metadata.collationSequence)
        assertTrue(!metadata.isNullable)
        assertTrue(metadata.isPrimaryKey)
        assertTrue(metadata.isAutoIncrement)
    }

    @Test
    fun serializeAndDeserializeWork() = runSqliteConnectionTest { sqlite, source ->
        source.execute("CREATE TABLE fruits(id INTEGER, name TEXT);")
        source.execute("INSERT INTO fruits VALUES (56, 'Framboise');")

        val result = source.serialize()
        val buffer = assertIs<SerializeResult.Mutable>(result).buffer

        sqlite.open(":memory:").use { destination ->
            destination.deserialize(
                serializedDatabase = buffer,
                databaseSize = result.databaseSize,
                flags = READONLY
            )

            var called = false

            destination.execute("SELECT id, name FROM fruits;") { _, values, _ ->
                assertEquals("56", values[0])
                assertEquals("Framboise", values[1])
                called = true
                false
            }

            assertTrue(called)

            assertFailsWith<SQLiteException> {
                destination.execute("INSERT INTO fruits VALUES (10, 'Mangue');")
            }
        }
    }

    @Test
    fun transactionStateWorks() = runSqliteConnectionTest { _, connection ->
        assertEquals(NONE, connection.getTransactionState())

        connection.execute("BEGIN;")
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        assertEquals(WRITE, connection.getTransactionState())
        connection.execute("COMMIT;")

        assertFailsWith<SQLiteException> {
            connection.getTransactionState("does_not_exist")
        }
    }

    @Test
    fun flushCacheWorks() = runSqliteConnectionTest { _, connection ->
        connection.flushCache()
    }

    @Test
    fun extendedResultCodesCanBeToggled() = runSqliteConnectionTest { _, connection ->
        connection.setExtendedResultCodesEnabled(true)
        connection.setExtendedResultCodesEnabled(false)
    }

    @Test
    fun writeAheadLogWorks() = runSqliteWalFileTest("wal-connection.db") { sqlite, path ->
        sqlite.open(path).use { connection ->
            connection.execute("PRAGMA journal_mode=WAL;")
            connection.execute("CREATE TABLE fruits(id INTEGER);")

            connection.wal.autoCheckpoint(1_000)

            var hookCalled = false

            connection.wal.setHook { _, databaseName, pageCount ->
                hookCalled = true
                assertEquals("main", databaseName)
                assertTrue(pageCount > 0)
            }

            connection.execute("INSERT INTO fruits VALUES (1);")
            assertTrue(hookCalled)

            val checkpointResult = connection.wal.checkpoint()
            assertTrue(checkpointResult.frameCount >= 0)
            assertTrue(checkpointResult.checkpointedFrameCount >= 0)

            connection.wal.setHook(null)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed connection violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceClosed() = runSqliteConnectionTest { _, connection ->
        connection.close()

        assertFailsWith<IllegalStateException> { connection.changes }
        assertFailsWith<IllegalStateException> { connection.isAutocommit }
        assertFailsWith<IllegalStateException> { connection.isInterrupted }
        assertFailsWith<IllegalStateException> { connection.lastInsertRowid }
        assertFailsWith<IllegalStateException> { connection.lastInsertRowid = 1 }
        assertFailsWith<IllegalStateException> { connection.totalChanges }
        assertFailsWith<IllegalStateException> { connection.setAutovacuumPages(null) }
        assertFailsWith<IllegalStateException> { connection.openBlob("test", "col", 1) }
        assertFailsWith<IllegalStateException> { connection.setBusyHandler(null) }
        assertFailsWith<IllegalStateException> { connection.setBusyTimeout(0) }
        assertFailsWith<IllegalStateException> { connection.setCollationNeeded(null) }
        assertFailsWith<IllegalStateException> { connection.setCommitHook(null) }
        assertFailsWith<IllegalStateException> { connection.deleteModule("does_not_exist") }
        assertFailsWith<IllegalStateException> { connection.flushCache() }
        assertFailsWith<IllegalStateException> { connection.getFileName() }
        assertFailsWith<IllegalStateException> { connection.getName(0) }
        assertFailsWith<IllegalStateException> { connection.isReadOnly("main") }
        assertFailsWith<IllegalStateException> { connection.releaseMemory() }
        assertFailsWith<IllegalStateException> { connection.getStatus(CACHE_USED) }
        assertFailsWith<IllegalStateException> { connection.setExtendedResultCodesEnabled(true) }
        assertFailsWith<IllegalStateException> { connection.execute("SELECT 1;") }
        assertFailsWith<IllegalStateException> { connection.interrupt() }
        assertFailsWith<IllegalStateException> { connection.getLimit(LENGTH) }
        assertFailsWith<IllegalStateException> { connection.setLimit(LENGTH, 0) }
        assertFailsWith<IllegalStateException> { connection.prepare("SELECT 1;") }
        assertFailsWith<IllegalStateException> { connection.setPreupdateHook(null) }
        assertFailsWith<IllegalStateException> { connection.setProgressHandler(1, null) }
        assertFailsWith<IllegalStateException> { connection.setRollbackHook(null) }
        assertFailsWith<IllegalStateException> { connection.serialize() }
        assertFailsWith<IllegalStateException> { connection.setAuthorizer(null) }
        assertFailsWith<IllegalStateException> { connection.createSnapshot() }
        assertFailsWith<IllegalStateException> { connection.tableColumnMetadata("t", "c") }
        assertFailsWith<IllegalStateException> { connection.setTrace(null, null) }
        assertFailsWith<IllegalStateException> { connection.getTransactionState() }
        assertFailsWith<IllegalStateException> { connection.setUpdateHook(null) }
        assertFailsWith<IllegalStateException> { connection.config.isForeignKeyEnabled }
        assertFailsWith<IllegalStateException> { connection.lastError.message }
        assertFailsWith<IllegalStateException> { connection.fileControl.getDataVersion() }
        assertFailsWith<IllegalStateException> { connection.wal.autoCheckpoint(1) }
    }
}
