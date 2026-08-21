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
package ksqlite.kapi.statement

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.runSqliteConnectionTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests [PreparedStatement].
 */
class PreparedStatementTest {

    @Test
    fun sqlPropertiesWork() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?;").use { statement ->
            assertEquals("SELECT ?;", statement.sql)
            assertEquals("SELECT NULL;", statement.expandedSql)

            statement.parameters.bindInt(1, 42)
            assertEquals("SELECT 42;", statement.expandedSql)

            assertEquals(1, statement.columnCount)
            assertTrue(statement.isReadOnly)
        }
    }

    @Test
    fun stepAndResetWork() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        connection.execute("INSERT INTO fruits VALUES (1), (2);")

        connection.prepare("SELECT id FROM fruits ORDER BY id;").use { statement ->
            assertTrue(!statement.isBusy)

            val firstRow = assertNotNull(statement.step())
            assertTrue(statement.isBusy)
            assertEquals(1, firstRow.getInt(0))

            val secondRow = assertNotNull(statement.step())
            assertEquals(2, secondRow.getInt(0))

            assertNull(statement.step())
            assertTrue(!statement.isBusy)

            statement.reset()
            assertTrue(!statement.isBusy)
            assertNotNull(statement.step())
        }
    }

    @Test
    fun forEachRowWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        connection.execute("INSERT INTO fruits VALUES (1), (2), (3);")

        val ids = mutableListOf<Int>()

        connection.prepare("SELECT id FROM fruits ORDER BY id;").use { statement ->
            statement.forEachRow { row ->
                ids.add(row.getInt(0))
            }
        }

        assertEquals(listOf(1, 2, 3), ids)
    }

    @Test
    fun forEachRowOnEmptyResultDoesNotInvokeAction() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")

        var callCount = 0

        connection.prepare("SELECT id FROM fruits;").use { statement ->
            statement.forEachRow { callCount++ }
        }

        assertEquals(0, callCount)
    }

    @Test
    fun forEachRowRowIsStaleOnceActionReturns() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        connection.execute("INSERT INTO fruits VALUES (1), (2);")

        var escapedRow: Row? = null

        connection.prepare("SELECT id FROM fruits ORDER BY id;").use { statement ->
            statement.forEachRow { row ->
                if (escapedRow == null) {
                    escapedRow = row
                }
            }
        }

        assertFailsWith<IllegalStateException> { assertNotNull(escapedRow).getInt(0) }
    }

    @Test
    fun stepFailsOnConstraintViolation() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY);")
        connection.execute("INSERT INTO fruits VALUES (1);")

        val statement = connection.prepare("INSERT INTO fruits VALUES (1);")

        assertFailsWith<SQLiteException> {
            statement.step()
        }

        // sqlite3_finalize surfaces the error of the most recently failed evaluation, so closing
        // the statement right after a failed step also throws.
        assertFailsWith<SQLiteException> {
            statement.close()
        }
    }

    @Test
    fun explainWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT 1;").use { statement ->
            assertEquals(NORMAL, statement.explain)

            statement.explain = EXPLAIN
            assertEquals(EXPLAIN, statement.explain)
        }
    }

    @Test
    fun getStatusWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT 1;").use { statement ->
            val status = statement.getStatus(RUN, reset = false)
            assertTrue(status >= 0)
        }
    }

    @Test
    fun closingOwningConnectionClosesStatement() = runSqliteConnectionTest { _, connection ->
        val statement = connection.prepare("SELECT 1;")
        connection.close()

        assertFailsWith<IllegalStateException> { statement.step() }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed statement violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceClosed() = runSqliteConnectionTest { _, connection ->
        val statement = connection.prepare("SELECT 1;")
        statement.close()
        // Closing again is a no-op
        statement.close()

        assertFailsWith<IllegalStateException> { statement.parameters.count }
        assertFailsWith<IllegalStateException> { statement.columnCount }
        assertFailsWith<IllegalStateException> { statement.expandedSql }
        assertFailsWith<IllegalStateException> { statement.sql }
        assertFailsWith<IllegalStateException> { statement.isBusy }
        assertFailsWith<IllegalStateException> { statement.explain }
        assertFailsWith<IllegalStateException> { statement.explain = NORMAL }
        assertFailsWith<IllegalStateException> { statement.isReadOnly }
        assertFailsWith<IllegalStateException> { statement.step() }
        assertFailsWith<IllegalStateException> { statement.getStatus(RUN, false) }
        assertFailsWith<IllegalStateException> { statement.reset() }
    }
}
