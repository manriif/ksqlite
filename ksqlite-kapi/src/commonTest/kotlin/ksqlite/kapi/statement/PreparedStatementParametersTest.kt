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

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.types.SqliteTextEncoding
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests [PreparedStatementParameters].
 */
class PreparedStatementParametersTest {

    @Test
    fun parameterMetadataWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT :a, :b, ?3;").use { statement ->
            val parameters = statement.parameters

            assertEquals(3, parameters.count)
            assertEquals(1, parameters.getIndex(":a"))
            assertEquals(2, parameters.getIndex(":b"))
            assertEquals(":a", parameters.getName(1))
            assertEquals(":b", parameters.getName(2))
            // Explicitly numbered parameters (?NNN) do have a name, unlike anonymous "?"
            assertEquals("?3", parameters.getName(3))
        }
    }

    @Test
    fun bindingScalarTypesWork() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?, ?, ?, ?, ?;").use { statement ->
            val parameters = statement.parameters

            parameters.bind(1, 42)
            parameters.bind(2, 42L)
            parameters.bind(3, 4.2)
            parameters.bind(4, "hello")
            parameters.bind(5, byteArrayOf(1, 2, 3))

            val row = assertNotNull(statement.step())
            assertEquals(42, row.getInt(0))
            assertEquals(42L, row.getLong(1))
            assertEquals(4.2, row.getDouble(2))
            assertEquals("hello", row.getString(3))
            assertContentEquals(byteArrayOf(1, 2, 3), row.getByteArray(4))
        }
    }

    @Test
    fun bindingNullWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?;").use { statement ->
            statement.parameters.bind(1, null)
            val row = assertNotNull(statement.step())
            assertNull(row.getString(0))
        }
    }

    @Test
    fun bindingNullableExtensionsWork() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?, ?, ?, ?;").use { statement ->
            val parameters = statement.parameters
            val text: String? = null
            val number: Int? = null

            parameters.bind(1, text)
            parameters.bind(2, number)
            parameters.bind(3, "present")
            parameters.bind(4, 7)

            val row = assertNotNull(statement.step())
            assertNull(row.getString(0))
            assertNull(row.getString(1))
            assertEquals("present", row.getString(2))
            assertEquals(7, row.getInt(3))
        }
    }

    @Test
    fun bindingZeroBlobWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?;").use { statement ->
            statement.parameters.bind(1, null, size = 4)
            val row = assertNotNull(statement.step())
            assertContentEquals(byteArrayOf(0, 0, 0, 0), row.getByteArray(0))
        }
    }

    @Test
    fun bindingBufferWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?;").use { statement ->
            val buffer = Buffer.allocate(3)
            buffer.write(byteArrayOf(9, 8, 7), size = 3)

            var cleanedUp = false
            statement.parameters.bind(1, buffer) { cleanedUp = true }

            val row = assertNotNull(statement.step())
            assertContentEquals(byteArrayOf(9, 8, 7), row.getByteArray(0))

            statement.reset()
            statement.parameters.clear()
            assertTrue(cleanedUp)

            buffer.close()
        }
    }

    @Test
    fun bindingTextBufferWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?;").use { statement ->
            val text = "hello"
            val buffer = Buffer.allocate(text.length)
            buffer.write(text.encodeToByteArray(), size = text.length)

            statement.parameters.bind(1, buffer, SqliteTextEncoding.UTF8)

            val row = assertNotNull(statement.step())
            assertEquals(text, row.getString(0))

            statement.reset()
            statement.parameters.clear()
            buffer.close()
        }
    }

    @Test
    fun bindingValueWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?;").use { source ->
            source.parameters.bind(1, "from-value")
            val row = assertNotNull(source.step())
            val value = assertNotNull(row.getValue(0))

            connection.prepare("SELECT ?;").use { destination ->
                destination.parameters.bind(1, value)
                val destinationRow = assertNotNull(destination.step())
                assertEquals("from-value", destinationRow.getString(0))
            }
        }
    }

    @Test
    fun clearWorks() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?;").use { statement ->
            statement.parameters.bind(1, "present")
            statement.parameters.clear()

            val row = assertNotNull(statement.step())
            assertNull(row.getString(0))
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed statement violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceStatementClosed() = runSqliteConnectionTest { _, connection ->
        val statement = connection.prepare("SELECT ?;")
        val parameters = statement.parameters
        statement.close()

        assertFailsWith<IllegalStateException> { parameters.count }
        assertFailsWith<IllegalStateException> { parameters.getIndex("?") }
        assertFailsWith<IllegalStateException> { parameters.getName(1) }
        assertFailsWith<IllegalStateException> { parameters.bind(1, null) }
        assertFailsWith<IllegalStateException> { parameters.bind(1, 1) }
        assertFailsWith<IllegalStateException> { parameters.bind(1, "text") }
        assertFailsWith<IllegalStateException> { parameters.clear() }
    }
}
