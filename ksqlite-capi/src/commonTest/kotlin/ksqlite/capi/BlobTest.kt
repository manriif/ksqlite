package ksqlite.capi

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests the BLBO APIs.
 */
class BlobTest {

    @Test
    fun itWorks() = runSqliteConnectionDataTest { db ->
        val insertSql = """
            INSERT INTO test VALUES 
                (0, 0.0, '', zeroblob(5), zeroblob(1)),
                (0, 0.0, '', zeroblob(5), zeroblob(1));
        """.trimIndent()

        val insertResult = sqlite3_exec(db, insertSql, null, null, null)
        assertEquals(OK, insertResult)

        val outBlob = sqlite3_blob.OutputParam()

        val blobOpenResult = sqlite3_blob_open(
            db = db,
            database = "main",
            tableName = "test",
            columnName = "blob_t",
            rowid = 1,
            flags = READWRITE,
            outBlob = outBlob
        )

        assertEquals(OK, blobOpenResult)

        val blob = assertNotNull(outBlob.value)
        val blob1Size = sqlite3_blob_bytes(blob)
        assertEquals(5, blob1Size)

        val actualBytes = ByteArray(5) { -1 }
        val readBlobResult = sqlite3_blob_read(blob, actualBytes, 5, 0)
        assertEquals(OK, readBlobResult)

        val expectedReadBytes = byteArrayOf(0, 0, 0, 0, 0)
        assertContentEquals(expectedReadBytes, actualBytes)

        val newBytes = ByteArray(5, Int::toByte)
        val writeBlobResult = sqlite3_blob_write(blob, newBytes, 3, 2)
        assertEquals(OK, writeBlobResult)

        val writtenBytes = ByteArray(5) { -1 }
        val readWrittenBytesResult = sqlite3_blob_read(blob, writtenBytes, 5, 0)
        assertEquals(OK, readWrittenBytesResult)

        val expectedWrittenBytes = byteArrayOf(0, 0, 0, 1, 2)
        assertContentEquals(expectedWrittenBytes, writtenBytes)

        val moveBlobResult = sqlite3_blob_reopen(blob, 2)
        assertEquals(OK, moveBlobResult)

        val blobCloseResult = sqlite3_blob_close(blob)
        assertEquals(OK, blobCloseResult)
    }
}