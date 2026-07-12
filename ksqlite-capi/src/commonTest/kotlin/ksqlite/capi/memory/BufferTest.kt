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
package ksqlite.capi.memory

import ksqlite.capi.runSqliteTest
import ksqlite.capi.sqlite3_free
import ksqlite.capi.sqlite3_malloc
import ksqlite.capi.sqlite3_malloc64
import ksqlite.capi.sqlite3_msize
import ksqlite.capi.sqlite3_randomness
import ksqlite.capi.sqlite3_realloc
import ksqlite.capi.sqlite3_realloc64
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test the [Buffer] interface.
 */
class BufferTest {

    @Test
    fun mallocAndReallocWorks() = runSqliteTest {
        val buffer = assertNotNull(sqlite3_malloc(16))
        assertEquals(16, buffer.byteSize)

        val mSize = sqlite3_msize(buffer)
        assertTrue(mSize >= 16UL)

        val content = ByteArray(16) { it.toByte() }
        buffer.write(content)

        val nativeContent = buffer.readBytes()
        assertContentEquals(content, nativeContent)

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

        val nativeContent = buffer.readBytes()
        assertContentEquals(content, nativeContent)

        val reallocBuffer = assertNotNull(sqlite3_realloc64(buffer, 64))
        assertEquals(64, reallocBuffer.byteSize)

        val halfContent = reallocBuffer.read(32)
        assertContentEquals(content, halfContent)

        val preRandomContent = reallocBuffer.readBytes()
        sqlite3_randomness(64, reallocBuffer)

        // Well preRandomContent's first 32 bytes are N = N, if the next assertion fails because
        // SQLite randomized the same suite of bytes then I stop programming
        val randomContent = reallocBuffer.readBytes()
        assertNotEquals(preRandomContent, randomContent)

        sqlite3_free(reallocBuffer)
    }
}