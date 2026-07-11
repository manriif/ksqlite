package ksqlite.kapi.blob

import ksqlite.capi.sqlite3_blob_bytes
import ksqlite.capi.sqlite3_blob_close
import ksqlite.capi.sqlite3_blob_read
import ksqlite.capi.sqlite3_blob_reopen
import ksqlite.capi.sqlite3_blob_write
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_blob
import ksqlite.kapi.helpers.UnsafeClosableScope
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.helpers.sqliteResultCheck

internal class BlobImpl(
    private val db: sqlite3,
    private val blob: sqlite3_blob
) : Blob,
    UnsafeClosableScope() {

    override val bytes: Int
        get() = notClosed { sqlite3_blob_bytes(blob) }

    override fun read(output: ByteArray, size: Int, offset: Int) =
        notClosed { sqliteResultCheck(sqlite3_blob_read(blob, output, size, offset)) }

    override fun write(input: ByteArray, size: Int, offset: Int) =
        notClosed { sqliteResultCheck(sqlite3_blob_write(blob, input, size, offset)) }

    override fun reopen(rowid: Long) =
        notClosed { db.resultCheck(sqlite3_blob_reopen(blob, rowid)) }

    override fun onClose() = db.resultCheck(sqlite3_blob_close(blob))
}