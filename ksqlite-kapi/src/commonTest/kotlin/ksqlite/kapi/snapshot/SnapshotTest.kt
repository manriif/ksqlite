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
package ksqlite.kapi.snapshot

import ksqlite.kapi.runSqliteWalFileTest
import ksqlite.kapi.connection.DatabaseConnection
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests [Snapshot], [DatabaseConnection.createSnapshot], [DatabaseConnection.openSnapshot] and
 * [DatabaseConnection.recoverSnapshots].
 */
class SnapshotTest {

    @Test
    fun comparisonWorks() = runSqliteWalFileTest("kapi-snapshot-compare.db") { sqlite, path ->
        val connection = sqlite.open(path)

        connection.execute(
            """
                PRAGMA journal_mode=WAL;
                CREATE TABLE test(id INTEGER);
                INSERT INTO test VALUES (1);
            """.trimIndent()
        )

        connection.execute("BEGIN;")
        val snapshot1 = connection.createSnapshot()
        assertEquals(0, snapshot1.compareTo(snapshot1))
        connection.execute("COMMIT;")

        connection.execute("INSERT INTO test VALUES (2);")

        connection.execute("BEGIN;")
        val snapshot2 = connection.createSnapshot()
        connection.execute("COMMIT;")

        assertTrue(snapshot1 < snapshot2)
        assertTrue(snapshot2 > snapshot1)

        snapshot1.close()
        snapshot2.close()
        connection.close()
    }

    @Test
    fun openAndRecoverWork() = runSqliteWalFileTest("kapi-snapshot-open.db") { sqlite, path ->
        val connection1 = sqlite.open(path)
        connection1.execute("PRAGMA journal_mode=WAL;")

        val connection2 = sqlite.open(path)
        connection2.execute("PRAGMA journal_mode=WAL;")

        connection1.execute("CREATE TABLE test(id INTEGER);")
        connection1.execute("INSERT INTO test VALUES (1);")
        connection1.execute("BEGIN;")

        val snapshot = connection1.createSnapshot()
        connection1.execute("COMMIT;")

        connection2.execute("INSERT INTO test VALUES (2), (3);")

        connection1.execute("BEGIN;")

        val allValues = mutableListOf<String>()

        connection1.execute("SELECT id FROM test;") { _, values, _ ->
            values[0]?.let(allValues::add)
            false
        }

        assertContentEquals(listOf("1", "2", "3"), allValues)

        connection1.openSnapshot(snapshot)

        val historicalValues = mutableListOf<String>()

        connection1.execute("SELECT id FROM test;") { _, values, _ ->
            values[0]?.let(historicalValues::add)
            false
        }

        assertContentEquals(listOf("1"), historicalValues)

        connection1.execute("COMMIT;")
        connection2.recoverSnapshots()

        snapshot.close()
        connection1.close()
        connection2.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed snapshot violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceClosed() = runSqliteWalFileTest("kapi-snapshot-closed.db") { sqlite, path ->
        val connection = sqlite.open(path)
        connection.execute("PRAGMA journal_mode=WAL;")
        connection.execute("CREATE TABLE test(id INTEGER);")

        connection.execute("BEGIN;")
        val snapshot = connection.createSnapshot()
        connection.execute("COMMIT;")

        snapshot.close()
        // Closing again is a no-op
        snapshot.close()

        assertFailsWith<IllegalStateException> { snapshot.compareTo(snapshot) }

        connection.close()
    }
}
