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

import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_blob
import ksqlite.capi.sqlite3_blob_bytes
import ksqlite.capi.sqlite3_blob_close
import ksqlite.capi.sqlite3_blob_read
import ksqlite.capi.sqlite3_blob_reopen
import ksqlite.capi.sqlite3_blob_write
import ksqlite.internal.runtime.closeable.UnsafeCloseableScope
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.helpers.sqliteResultCheck

internal class BlobImpl(
    private val db: sqlite3,
    private val blob: sqlite3_blob
) : Blob,
    UnsafeCloseableScope() {

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