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

/**
 * Streams the content of a single BLOB value in and out without loading it fully into memory,
 * see the [BLOB API](https://sqlite.org/c3ref/blob.html).
 *
 * Unless documented otherwise, every member throws [IllegalStateException] once this blob is
 * closed.
 */
public interface Blob : AutoCloseable {

    /**
     * Size of this blob in bytes. This does not change over the life of this instance, reopen
     * this blob with [reopen] to observe a value written concurrently by another connection.
     */
    public val bytes: Int

    /**
     * Reads [size] bytes from this blob, starting at [offset], into [output].
     *
     * @throws ksqlite.kapi.SQLiteException if [offset] and [size] are out of range for this blob,
     * or if the read fails.
     */
    public fun read(
        output: ByteArray,
        size: Int = output.size,
        offset: Int = 0
    )

    /**
     * Writes [size] bytes from [input] into this blob, starting at [offset].
     *
     * This can only overwrite existing bytes, it cannot grow the blob. [offset] plus [size] must
     * not exceed [bytes].
     *
     * @throws ksqlite.kapi.SQLiteException if [offset] and [size] are out of range for this blob,
     * or if the write fails.
     */
    public fun write(
        input: ByteArray,
        size: Int = input.size,
        offset: Int = 0
    )

    /**
     * Points this blob at the row identified by [rowid], in the same database, table and column
     * it was originally opened on. This is cheaper than closing and reopening a new [Blob].
     *
     * @throws ksqlite.kapi.SQLiteException if [rowid] does not exist or the move fails.
     */
    public fun reopen(rowid: Long)

    /**
     * Closes this blob, releasing the resources it holds. Calling this again on an already closed
     * blob has no effect.
     *
     * @throws ksqlite.kapi.SQLiteException if closing fails.
     */
    override fun close()
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Reads [size] bytes from this blob, starting at [offset], and returns them as a new [ByteArray].
 *
 * @throws ksqlite.kapi.SQLiteException if [offset] and [size] are out of range for this blob,
 * or if the read fails.
 */
public fun Blob.read(
    size: Int = bytes,
    offset: Int = 0
): ByteArray = ByteArray(size).apply {
    read(this, size, offset)
}