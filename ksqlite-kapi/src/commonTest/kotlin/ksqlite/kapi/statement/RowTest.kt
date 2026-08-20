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

import ksqlite.kapi.buffer.readBytes
import ksqlite.kapi.runSqliteConnectionTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests [Row].
 */
class RowTest {

    @Test
    fun columnAccessorsWork() = runSqliteConnectionTest { _, connection ->
        connection.execute(
            """
                CREATE TABLE fruits(id INTEGER, weight REAL, name TEXT, image BLOB, empty TEXT);
                INSERT INTO fruits VALUES (56, 1.5, 'Framboise', X'010203', NULL);
            """.trimIndent()
        )

        connection.prepare("SELECT * FROM fruits;").use { statement ->
            val row = assertNotNull(statement.step())

            assertEquals(5, row.dataCount)

            assertEquals("main", row.getDatabaseName(0))
            assertEquals("fruits", row.getTableName(0))
            assertEquals("id", row.getColumnOriginName(0))
            assertEquals("id", row.getColumnName(0))
            assertEquals("INTEGER", row.getDeclaredType(0))

            assertEquals(INTEGER, row.getType(0))
            assertEquals(56, row.getInt(0))
            assertEquals(56L, row.getLong(0))
            assertEquals("56", row.getString(0))

            assertEquals(FLOAT, row.getType(1))
            assertEquals(1.5, row.getDouble(1))

            assertEquals(TEXT, row.getType(2))
            assertEquals("Framboise", row.getString(2))
            assertEquals(9, row.getSize(2))

            assertEquals(BLOB, row.getType(3))
            assertContentEquals(byteArrayOf(1, 2, 3), row.getByteArray(3))
            assertContentEquals(byteArrayOf(1, 2, 3), row.getBuffer(3)?.readBytes())

            assertEquals(NULL, row.getType(4))
            assertNull(row.getString(4))

            val value = assertNotNull(row.getValue(0))
            val duplicated = value.duplicate()
            assertEquals(56, duplicated.getAsInt())
            duplicated.close()
        }
    }

    @Test
    fun rowBecomesStaleOnNextStep() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        connection.execute("INSERT INTO fruits VALUES (1), (2);")

        connection.prepare("SELECT id FROM fruits ORDER BY id;").use { statement ->
            val firstRow = assertNotNull(statement.step())
            assertEquals(1, firstRow.getInt(0))

            statement.step()

            assertFailsWith<IllegalStateException> { firstRow.getInt(0) }
        }
    }

    @Test
    fun rowBecomesStaleOnReset() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        connection.execute("INSERT INTO fruits VALUES (1);")

        connection.prepare("SELECT id FROM fruits;").use { statement ->
            val row = assertNotNull(statement.step())
            statement.reset()

            assertFailsWith<IllegalStateException> { row.getInt(0) }
        }
    }

    @Test
    fun rowBecomesStaleOnStatementClose() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER);")
        connection.execute("INSERT INTO fruits VALUES (1);")

        val statement = connection.prepare("SELECT id FROM fruits;")
        val row = assertNotNull(statement.step())
        statement.close()

        assertFailsWith<IllegalStateException> { row.getInt(0) }
    }
}
