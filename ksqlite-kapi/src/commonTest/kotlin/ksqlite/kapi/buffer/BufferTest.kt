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
package ksqlite.kapi.buffer

import ksqlite.kapi.runSqliteConnectionTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

/**
 * Tests [Buffer] and [ReadableBuffer].
 */
class BufferTest {

    @Test
    fun allocateAndReadWriteWork() {
        val buffer = Buffer.allocate(4)
        assertEquals(4, buffer.byteSize)

        buffer.write(byteArrayOf(1, 2, 3, 4), size = 4)
        assertContentEquals(byteArrayOf(1, 2, 3, 4), buffer.readBytes())
        assertContentEquals(byteArrayOf(1, 2, 3, 4), buffer.readBytesOrThrow())
        assertContentEquals(byteArrayOf(1, 2, 3, 4), buffer.read(4))

        val destination = ByteArray(2)
        buffer.read(destination, size = 2, sourceOffset = 1)
        assertContentEquals(byteArrayOf(2, 3), destination)

        buffer.close()
    }

    @Test
    fun writeWithOffsetsWorks() {
        val buffer = Buffer.allocate(4)
        buffer.write(byteArrayOf(0, 0, 0, 0), size = 4)
        buffer.write(byteArrayOf(9, 9, 9), size = 2, sourceOffset = 1, destinationOffset = 1)

        assertContentEquals(byteArrayOf(0, 9, 9, 0), buffer.readBytes())
        buffer.close()
    }

    @Test
    fun allocateWithLongSizeWorks() {
        val buffer = Buffer.allocate(4L)
        assertEquals(4L, buffer.byteSize)
        buffer.close()
    }

    @Test
    fun resizeWorks() {
        val buffer = Buffer.allocate(2)
        buffer.write(byteArrayOf(5, 6), size = 2)

        buffer.resize(4)
        assertEquals(4, buffer.byteSize)

        buffer.resize(2L)
        assertEquals(2L, buffer.byteSize)

        buffer.close()
    }

    @Test
    fun resizeRejectsNonPositiveSize() {
        val buffer = Buffer.allocate(2)

        assertFailsWith<IllegalArgumentException> { buffer.resize(0) }
        assertFailsWith<IllegalArgumentException> { buffer.resize(-1L) }

        buffer.close()
    }

    @Test
    fun bufferInUseByStatementCannotBeMutatedOrClosed() = runSqliteConnectionTest { _, connection ->
        connection.prepare("SELECT ?;").use { statement ->
            val buffer = Buffer.allocate(3)
            buffer.write(byteArrayOf(1, 2, 3), size = 3)

            statement.parameters.bind(1, buffer)

            assertFailsWith<BufferInUseException> { buffer.close() }
            assertFailsWith<BufferInUseException> { buffer.write(byteArrayOf(9), size = 1) }
            assertFailsWith<BufferInUseException> { buffer.resize(6) }

            // Clearing the binding releases SQLite's reference on the buffer.
            statement.parameters.clear()
            buffer.close()
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed buffer violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceClosed() {
        val buffer = Buffer.allocate(2)
        buffer.close()
        // Closing again is a no-op
        buffer.close()

        assertFailsWith<IllegalStateException> { buffer.byteSize }
        assertFailsWith<IllegalStateException> { buffer.read(ByteArray(1)) }
        assertFailsWith<IllegalStateException> { buffer.read(1) }
        assertFailsWith<IllegalStateException> { buffer.readBytes() }
        assertFailsWith<IllegalStateException> { buffer.readBytesOrThrow() }
        assertFailsWith<IllegalStateException> { buffer.write(byteArrayOf(1), size = 1) }
        assertFailsWith<IllegalStateException> { buffer.resize(4) }
    }

    @Test
    fun readableBufferFromRowBecomesStaleWithRow() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(image BLOB);")
        connection.execute("INSERT INTO fruits VALUES (X'0102');")

        connection.prepare("SELECT image FROM fruits;").use { statement ->
            val row = assertNotNull(statement.step())
            val readableBuffer = assertNotNull(row.getBuffer(0))
            assertEquals(2, readableBuffer.byteSize)

            statement.reset()

            assertFailsWith<IllegalStateException> { readableBuffer.byteSize }
        }
    }
}
