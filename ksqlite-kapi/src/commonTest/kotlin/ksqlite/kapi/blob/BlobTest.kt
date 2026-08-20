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
package ksqlite.kapi.blob

import ksqlite.kapi.SQLiteException
import ksqlite.kapi.runSqliteConnectionTest
import ksqlite.types.SqliteBlobOpenFlag
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests [Blob].
 */
class BlobTest {

    @Test
    fun readWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY, image BLOB);")
        connection.execute("INSERT INTO fruits VALUES (1, X'0102030405');")

        connection.openBlob("fruits", "image", 1).use { blob ->
            assertEquals(5, blob.bytes)
            assertContentEquals(byteArrayOf(1, 2, 3, 4, 5), blob.read())

            val partial = ByteArray(2)
            blob.read(partial, size = 2, offset = 1)
            assertContentEquals(byteArrayOf(2, 3), partial)
        }
    }

    @Test
    fun writeWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY, image BLOB);")
        connection.execute("INSERT INTO fruits VALUES (1, X'0000000000');")

        connection.openBlob("fruits", "image", 1, flags = SqliteBlobOpenFlag.READWRITE).use { blob ->
            blob.write(byteArrayOf(9, 9), offset = 1)
        }

        connection.openBlob("fruits", "image", 1).use { blob ->
            assertContentEquals(byteArrayOf(0, 9, 9, 0, 0), blob.read())
        }
    }

    @Test
    fun writeFailsOnReadOnlyBlob() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY, image BLOB);")
        connection.execute("INSERT INTO fruits VALUES (1, X'0000');")

        val blob = connection.openBlob("fruits", "image", 1)

        assertFailsWith<SQLiteException> {
            blob.write(byteArrayOf(1, 2))
        }

        // sqlite3_blob_close surfaces the error of the most recently failed operation on the blob.
        assertFailsWith<SQLiteException> {
            blob.close()
        }
    }

    @Test
    fun reopenWorks() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY, image BLOB);")
        connection.execute("INSERT INTO fruits VALUES (1, X'0102'), (2, X'0304');")

        connection.openBlob("fruits", "image", 1).use { blob ->
            assertContentEquals(byteArrayOf(1, 2), blob.read())

            blob.reopen(2)
            assertContentEquals(byteArrayOf(3, 4), blob.read())
        }
    }

    @Test
    fun openBlobFailsForUnknownRow() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY, image BLOB);")

        assertFailsWith<SQLiteException> {
            connection.openBlob("fruits", "image", 999)
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Closed blob violations
    ///////////////////////////////////////////////////////////////////////////

    @Test
    fun operationsFailOnceClosed() = runSqliteConnectionTest { _, connection ->
        connection.execute("CREATE TABLE fruits(id INTEGER PRIMARY KEY, image BLOB);")
        connection.execute("INSERT INTO fruits VALUES (1, X'0102');")

        val blob = connection.openBlob("fruits", "image", 1, flags = SqliteBlobOpenFlag.READWRITE)
        blob.close()
        // Closing again is a no-op
        blob.close()

        assertFailsWith<IllegalStateException> { blob.bytes }
        assertFailsWith<IllegalStateException> { blob.read(ByteArray(1)) }
        assertFailsWith<IllegalStateException> { blob.write(byteArrayOf(1)) }
        assertFailsWith<IllegalStateException> { blob.reopen(1) }
    }
}
