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
package ksqlite.kapi.backup

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.runSqliteTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests [Backup].
 */
class BackupTest {

    @Test
    fun backupWorks() = runSqliteTest { sqlite ->
        val source = sqlite.open(":memory:")
        val destination = sqlite.open(":memory:")

        source.execute("CREATE TABLE fruits(id INTEGER, name TEXT);")
        source.execute("INSERT INTO fruits VALUES (1, 'Kiwi'), (2, 'Mango');")

        val backup = Backup.init(destination = destination, source = source)

        assertTrue(backup.pageCount >= 0)

        backup.step(-1)
        assertEquals(0, backup.remaining)
        assertTrue(backup.pageCount > 0)

        backup.close()

        var callCount = 0

        destination.execute("SELECT id, name FROM fruits ORDER BY id;") { _, values, _ ->
            callCount++
            assertNotNull(values[0])
            assertNotNull(values[1])
            false
        }

        assertEquals(2, callCount)

        source.close()
        destination.close()
    }

    @Test
    fun backupWithExplicitDatabaseNamesWorks() = runSqliteTest { sqlite ->
        val source = sqlite.open(":memory:")
        val destination = sqlite.open(":memory:")

        source.execute("CREATE TABLE fruits(id INTEGER);")
        source.execute("INSERT INTO fruits VALUES (1);")

        val backup = Backup.init(
            destination = destination,
            destinationName = "main",
            source = source,
            sourceName = "main"
        )

        backup.step(-1)
        backup.close()

        var count = 0
        destination.execute("SELECT id FROM fruits;") { _, _, _ -> count++; false }
        assertEquals(1, count)

        source.close()
        destination.close()
    }

    @Test
    fun stepInIncrementsWork() = runSqliteTest { sqlite ->
        val source = sqlite.open(":memory:")
        val destination = sqlite.open(":memory:")

        source.execute("CREATE TABLE fruits(id INTEGER);")

        repeat(50) { source.execute("INSERT INTO fruits VALUES ($it);") }

        val backup = Backup.init(destination = destination, source = source)
        var stepCount = 0

        while (backup.remaining > 0 || stepCount == 0) {
            backup.step(1)
            stepCount++

            if (stepCount > 10_000) {
                error("Backup did not complete in a reasonable number of steps")
            }
        }

        assertTrue(stepCount > 1)
        backup.close()

        source.close()
        destination.close()
    }

    @Test
    fun initFailsOnUnknownDatabaseName() = runSqliteTest { sqlite ->
        val source = sqlite.open(":memory:")
        val destination = sqlite.open(":memory:")

        assertFailsWith<SQLiteException> {
            Backup.init(
                destination = destination,
                destinationName = "does_not_exist",
                source = source,
                sourceName = "main"
            )
        }

        source.close()
        destination.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed backup violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceClosed() = runSqliteTest { sqlite ->
        val source = sqlite.open(":memory:")
        val destination = sqlite.open(":memory:")

        val backup = Backup.init(destination = destination, source = source)
        backup.close()
        // Closing again is a no-op
        backup.close()

        assertFailsWith<IllegalStateException> { backup.pageCount }
        assertFailsWith<IllegalStateException> { backup.remaining }
        assertFailsWith<IllegalStateException> { backup.step(1) }

        source.close()
        destination.close()
    }
}
