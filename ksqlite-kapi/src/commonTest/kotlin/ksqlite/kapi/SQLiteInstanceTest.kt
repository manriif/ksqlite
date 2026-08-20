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
package ksqlite.kapi

import ksqlite.kapi.connection.AutoExtension
import ksqlite.types.SqliteOpenFlag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests the [SQLite] lifecycle and the APIs it exposes once initialized.
 */
class SQLiteInstanceTest {

    @Test
    fun initializeAndCloseWorks() = runTestNoInit {
        val sqlite = SQLite.initialize()
        sqlite.close()
        // Closing again is a no-op
        sqlite.close()
    }

    @Test
    fun onlyOneInstanceCanExistAtOnce() = runTestNoInit {
        val sqlite = SQLite.initialize()

        assertFailsWith<IllegalStateException> {
            SQLite.initialize()
        }

        sqlite.close()

        // A new instance can be created once the previous one was closed
        val other = SQLite.initialize()
        other.close()
    }

    @Test
    fun configureIsAppliedBeforeInitialization() = runSqliteTest(
        configure = { isRowidInViewActivated = true }
    ) { sqlite ->
        assertTrue(sqlite.config.pageCacheHeaderSize >= 0)
    }

    @Test
    fun accessorsWork() = runSqliteTest { sqlite ->
        // Sub-managers are reachable and usable
        assertTrue(sqlite.virtualFileSystems.default != null)
        assertTrue(sqlite.ciphers.count >= 0)
    }

    @Test
    fun heapLimitsWork() = runSqliteTest { sqlite ->
        val hardLimit = sqlite.hardHeapLimit
        sqlite.hardHeapLimit = hardLimit
        assertEquals(hardLimit, sqlite.hardHeapLimit)

        val softLimit = sqlite.softHeapLimit
        sqlite.softHeapLimit = softLimit
        assertEquals(softLimit, sqlite.softHeapLimit)
    }

    @Test
    fun memoryWorks() = runSqliteTest { sqlite ->
        assertTrue(sqlite.memoryUsed >= 0)
        assertTrue(sqlite.memoryHighwater >= sqlite.memoryUsed)

        val status = sqlite.getMemoryStatus(reset = false)
        assertEquals(sqlite.memoryUsed, status.current)
        assertTrue(status.highwater >= status.current)

        val releasedMemory = sqlite.releaseMemory(0)
        assertEquals(0, releasedMemory)
    }

    @Test
    fun getStatusWorks() = runSqliteTest { sqlite ->
        val status = sqlite.getStatus(MEMORY_USED)
        assertEquals(sqlite.memoryUsed, status.current)
        assertTrue(status.highwater >= status.current)
    }

    @Test
    fun generateRandomBytesWorks() = runSqliteTest { sqlite ->
        val size = 32

        val bytes = sqlite.generateRandomBytes(size)
        assertEquals(size, bytes.size)

        val otherBytes = sqlite.generateRandomBytes(size)
        assertNotEquals(bytes.toList(), otherBytes.toList())
    }

    @Test
    fun openWorks() = runSqliteTest { sqlite ->
        val connection = sqlite.open(":memory:")
        connection.execute("CREATE TABLE fruits(name TEXT);")
        connection.close()
    }

    @Test
    fun openWithBadPathFails() = runSqliteTest { sqlite ->
        assertFailsWith<SQLiteException> {
            sqlite.open("db.sqlite", flags = SqliteOpenFlag.READWRITE)
        }
    }

    @Test
    fun autoExtensionsWork() = runSqliteTest { sqlite ->
        var applyCount = 0

        val extension = AutoExtension { connection ->
            connection.execute("CREATE TABLE auto_extension_marker(id INTEGER);")
            applyCount++
        }

        sqlite.addAutoExtension(extension)
        // Registering the same extension twice has no effect
        sqlite.addAutoExtension(extension)

        sqlite.open(":memory:").use { connection ->
            assertEquals(1, applyCount)
            connection.execute("INSERT INTO auto_extension_marker VALUES (1);")
        }

        sqlite.removeAutoExtension(extension)

        sqlite.open(":memory:").use {
            assertEquals(1, applyCount)
        }

        sqlite.addAutoExtension(extension)
        sqlite.clearAutoExtensions()

        sqlite.open(":memory:").use {
            assertEquals(1, applyCount)
        }
    }

    @Test
    fun autoExtensionFailureClosesConnectionAndPropagates() = runSqliteTest { sqlite ->
        val extension = AutoExtension {
            throwSQLiteException("Boom")
        }

        sqlite.addAutoExtension(extension)

        assertFailsWith<SQLiteException> {
            sqlite.open(":memory:")
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed instance violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceClosed() = runTestNoInit {
        val sqlite = SQLite.initialize()
        val extension = AutoExtension {}
        sqlite.close()

        assertFailsWith<IllegalStateException> { sqlite.config.pageCacheHeaderSize }
        assertFailsWith<IllegalStateException> { sqlite.ciphers.count }
        assertFailsWith<IllegalStateException> { sqlite.virtualFileSystems.default }
        assertFailsWith<IllegalStateException> { sqlite.hardHeapLimit }
        assertFailsWith<IllegalStateException> { sqlite.hardHeapLimit = 0 }
        assertFailsWith<IllegalStateException> { sqlite.memoryUsed }
        assertFailsWith<IllegalStateException> { sqlite.memoryHighwater }
        assertFailsWith<IllegalStateException> { sqlite.softHeapLimit }
        assertFailsWith<IllegalStateException> { sqlite.softHeapLimit = 0 }
        assertFailsWith<IllegalStateException> { sqlite.addAutoExtension(extension) }
        assertFailsWith<IllegalStateException> { sqlite.removeAutoExtension(extension) }
        assertFailsWith<IllegalStateException> { sqlite.clearAutoExtensions() }
        assertFailsWith<IllegalStateException> { sqlite.getMemoryStatus(false) }
        assertFailsWith<IllegalStateException> { sqlite.open(":memory:") }
        assertFailsWith<IllegalStateException> { sqlite.releaseMemory(0) }
        assertFailsWith<IllegalStateException> { sqlite.getStatus(MEMORY_USED) }
    }
}
