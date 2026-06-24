package ksqlite.capi.memory

import ksqlite.capi.runSqliteTest
import ksqlite.capi.sqlite3_free
import ksqlite.capi.sqlite3_malloc
import ksqlite.capi.sqlite3_malloc64
import ksqlite.capi.sqlite3_realloc
import ksqlite.capi.sqlite3_realloc64
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Test the [Buffer] interface.
 */
class BufferTest {

    @Test
    fun mallocAndReallocWorks() = runSqliteTest {
        val buffer = assertNotNull(sqlite3_malloc(16))
        assertEquals(16, buffer.byteSize)

        val content = ByteArray(16) { it.toByte() }
        buffer.write(content)

        assertContentEquals(content, buffer.readBytes())

        val reallocBuffer = assertNotNull(sqlite3_realloc(buffer, 32))
        assertEquals(32, reallocBuffer.byteSize)

        val halfContent = reallocBuffer.read(16)
        assertContentEquals(content, halfContent)

        sqlite3_free(reallocBuffer)
    }

    @Test
    fun malloc64AndRealloc64Works() = runSqliteTest {
        val buffer = assertNotNull(sqlite3_malloc64(32))
        assertEquals(32, buffer.byteSize)

        val content = ByteArray(32) { it.toByte() }
        buffer.write(content)

        assertContentEquals(content, buffer.readBytes())

        val reallocBuffer = assertNotNull(sqlite3_realloc64(buffer, 64))
        assertEquals(64, reallocBuffer.byteSize)

        val halfContent = reallocBuffer.read(32)
        assertContentEquals(content, halfContent)

        sqlite3_free(reallocBuffer)
    }
}