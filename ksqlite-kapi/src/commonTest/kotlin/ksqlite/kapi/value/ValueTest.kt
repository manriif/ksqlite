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
package ksqlite.kapi.value

import ksqlite.kapi.buffer.Buffer
import ksqlite.kapi.result.resultBoolean
import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.kapi.statement.bindBoolean
import ksqlite.kapi.statement.getBoolean
import ksqlite.types.SqliteTextEncoding
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests [ProtectedValue], [UnprotectedValue], [DuplicatedValue] and [ResultScope], all of
 * which can only be exercised through a live SQLite call (a bound parameter, a row, or a scalar
 * function invocation).
 */
class ValueTest {

    @Test
    fun protectedValueAccessorsWork() = runSqliteConnectionTest { _, connection ->
        var called = false

        connection.createFunction("probe", 1, SqliteTextEncoding.UTF8) { arguments ->
            val value = arguments[0]
            assertEquals(TEXT, value.type)
            assertEquals(TEXT, value.numericType)
            assertTrue(value.isFromBind)
            assertEquals(5, value.bytes)
            assertEquals("hello", value.getAsString())
            called = true
            resultInt(1)
        }

        connection.prepare("SELECT probe(?);").use { statement ->
            statement.parameters.bindString(1, "hello")
            statement.step()
        }

        assertTrue(called)
    }

    @Test
    fun protectedValueNumericAccessorsWork() = runSqliteConnectionTest { _, connection ->
        var called = false

        connection.createFunction("probe", 1, SqliteTextEncoding.UTF8) { arguments ->
            val value = arguments[0]
            assertEquals(INTEGER, value.type)
            assertEquals(42, value.getAsInt())
            assertEquals(42L, value.getAsLong())
            assertEquals(42.0, value.getAsDouble())
            called = true
            resultInt(1)
        }

        connection.prepare("SELECT probe(?);").use { statement ->
            statement.parameters.bindInt(1, 42)
            statement.step()
        }

        assertTrue(called)
    }

    @Test
    fun protectedValueBooleanAccessorWorks() = runSqliteConnectionTest { _, connection ->
        val seen = mutableListOf<Boolean>()

        val registered = connection.createFunction("probe", 1, SqliteTextEncoding.UTF8) { arguments ->
            seen.add(arguments[0].getAsBoolean())
            resultInt(1)
        }

        connection.prepare("SELECT probe(?);").use { statement ->
            statement.parameters.bindBoolean(1, true)
            statement.step()
        }

        registered.close()
        assertEquals(listOf(true), seen)
    }

    @Test
    fun protectedValueBooleanAccessorThrowsForValueOtherThanZeroOrOne() =
        runSqliteConnectionTest { _, connection ->
            var thrown = false

            val registered = connection.createFunction("probe", 1, SqliteTextEncoding.UTF8) { arguments ->
                thrown = runCatching { arguments[0].getAsBoolean() }.isFailure
                resultInt(1)
            }

            connection.prepare("SELECT probe(?);").use { statement ->
                statement.parameters.bindInt(1, 2)
                statement.step()
            }

            registered.close()
            assertTrue(thrown)
        }

    @Test
    fun protectedValueBlobAccessorsWork() = runSqliteConnectionTest { _, connection ->
        var called = false

        connection.createFunction("probe", 1, SqliteTextEncoding.UTF8) { arguments ->
            val value = arguments[0]
            assertEquals(BLOB, value.type)
            assertContentEquals(byteArrayOf(1, 2, 3), value.getAsByteArray())
            assertNotNull(value.getAsBuffer())
            called = true
            resultInt(1)
        }

        connection.prepare("SELECT probe(?);").use { statement ->
            statement.parameters.bindByteArray(1, byteArrayOf(1, 2, 3))
            statement.step()
        }

        assertTrue(called)
    }

    @Test
    fun duplicateWorks() = runSqliteConnectionTest { _, connection ->
        var duplicated: DuplicatedValue? = null

        connection.createFunction("probe", 1, SqliteTextEncoding.UTF8) { arguments ->
            duplicated = arguments[0].duplicate()
            resultInt(1)
        }

        connection.prepare("SELECT probe(?);").use { statement ->
            statement.parameters.bindInt(1, 7)
            statement.step()
        }

        val value = assertNotNull(duplicated)
        // Unlike ProtectedValue arguments, a DuplicatedValue outlives the function call.
        assertEquals(7, value.getAsInt())
        value.close()

        assertFailsWith<IllegalStateException> { value.getAsInt() }
    }

    @Test
    fun protectedValueBecomesStaleAfterCall() = runSqliteConnectionTest { _, connection ->
        var probed: ProtectedValue? = null

        connection.createFunction("probe", 1, SqliteTextEncoding.UTF8) {
            probed = it[0]
            resultInt(1)
        }

        connection.prepare("SELECT probe(?);").use { statement ->
            statement.parameters.bindInt(1, 7)
            statement.step()
        }

        val value = assertNotNull(probed)
        assertFailsWith<IllegalStateException> { value.getAsInt() }
    }

    @Test
    fun resultScopeVariantsWork() = runSqliteConnectionTest { _, connection ->
        connection.createFunction("as_null", 0, SqliteTextEncoding.UTF8) {
            resultNull()
        }

        connection.createFunction("as_int", 0, SqliteTextEncoding.UTF8) {
            resultInt(42)
        }

        connection.createFunction("as_long", 0, SqliteTextEncoding.UTF8) {
            resultLong(42L)
        }

        connection.createFunction("as_double", 0, SqliteTextEncoding.UTF8) {
            resultDouble(4.2)
        }

        connection.createFunction("as_text", 0, SqliteTextEncoding.UTF8) {
            resultString("hi")
        }

        connection.createFunction("as_blob", 0, SqliteTextEncoding.UTF8) {
            resultByteArray(byteArrayOf(1, 2, 3))
        }

        connection.createFunction("as_zeroblob", 0, SqliteTextEncoding.UTF8) {
            resultZeroBlob(size = 3)
        }

        connection.createFunction("as_bool", 0, SqliteTextEncoding.UTF8) {
            resultBoolean(true)
        }

        connection.prepare(
            """
                SELECT as_null(), as_int(), as_long(), as_double(), as_text(), as_blob(),
                       as_zeroblob(), as_bool();
            """.trimIndent()
        ).use { statement ->
            val row = assertNotNull(statement.step())
            assertEquals(NULL, row.getType(0))
            assertEquals(42, row.getInt(1))
            assertEquals(42L, row.getLong(2))
            assertEquals(4.2, row.getDouble(3))
            assertEquals("hi", row.getString(4))
            assertContentEquals(byteArrayOf(1, 2, 3), row.getByteArray(5))
            assertContentEquals(byteArrayOf(0, 0, 0), row.getByteArray(6))
            assertEquals(true, row.getBoolean(7))
        }
    }

    @Test
    fun resultScopeBufferVariantsWork() = runSqliteConnectionTest { _, connection ->
        connection.createFunction("as_buffer_blob", 0, SqliteTextEncoding.UTF8) {
            val buffer = Buffer.allocate(3)
            buffer.write(byteArrayOf(4, 5, 6), size = 3)
            resultBuffer(buffer)
        }

        connection.prepare("SELECT as_buffer_blob();").use { statement ->
            val row = assertNotNull(statement.step())
            assertContentEquals(byteArrayOf(4, 5, 6), row.getByteArray(0))
        }
    }
}
