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
@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.kapi.buffer

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.sqlite3_free
import ksqlite.capi.sqlite3_malloc
import ksqlite.capi.sqlite3_malloc64
import ksqlite.capi.sqlite3_realloc
import ksqlite.capi.sqlite3_realloc64
import ksqlite.internal.runtime.closeable.DelegatingAtomicCloseableScope
import ksqlite.kapi.helpers.sqliteOutOfMemoryCheck
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.decrementAndFetch
import kotlin.concurrent.atomics.incrementAndFetch
import ksqlite.capi.memory.Buffer as CapiBuffer

/**
 * Region of native memory that can be read and written.
 *
 * This class provides no thread-safety of its own. Synchronize externally if the same instance is
 * accessed from more than one thread.
 *
 * This buffer must be closed once no longer needed to release the memory it holds. Unless
 * documented otherwise, every member throws [IllegalStateException] once it is closed.
 */
public class Buffer internal constructor(capiBuffer: CapiBuffer) :
    ReadableBuffer(capiBuffer),
    AutoCloseable {

    private val refCount = AtomicInt(0)

    override var buffer: CapiBuffer = capiBuffer
        private set

    override val scope = DelegatingAtomicCloseableScope { sqlite3_free(buffer) }

    /**
     * Ensures that the buffer is not referenced by SQLite.
     */
    private fun ensureNotReferenced() {
        if (refCount.load() != 0) {
            throw BufferInUseException("The buffer is currently in use by SQLite")
        }
    }

    /**
     * Increments a counter that tracks the number of active buffer referencer and returns an
     * [SqliteDestroyCallback] that will decrement the counter after the owner ended using it.
     *
     * The returned [SqliteDestroyCallback] must be invoked exactly once.
     */
    internal fun reference(
        notify: ((Buffer) -> Unit)?
    ): SqliteDestroyCallback<CapiBuffer> = scope.notClosed {
        val _ = refCount.incrementAndFetch()

        SqliteDestroyCallback { capiBuffer ->
            check(capiBuffer === buffer)
            val _ = refCount.decrementAndFetch()
            notify?.invoke(this)
        }
    }

    /**
     * Writes [size] bytes from [source] into the native memory block.
     *
     * Reading starts at [sourceOffset] in [source], and writing starts at [destinationOffset] in
     * the native memory region.
     *
     * @throws IllegalArgumentException if [size], [sourceOffset], or [destinationOffset] is
     * negative.
     * @throws IndexOutOfBoundsException if the requested range is out of bounds in either [source]
     * or the native memory block.
     * @throws BufferInUseException if SQLite is currently borrowing this buffer, for example as a
     * bound statement parameter.
     */
    public fun write(
        source: ByteArray,
        size: Int,
        sourceOffset: Int = 0,
        destinationOffset: Long = 0
    ): Unit = scope.notClosed {
        ensureNotReferenced()

        buffer.write(
            source = source,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }

    /**
     * Updates the [buffer] if resizing succeeded.
     */
    private inline fun resize(
        newSize: Long,
        block: () -> CapiBuffer?
    ) {
        require(newSize > 0L) { "Size must be greater than 0 ($newSize requested)" }

        scope.notClosed {
            ensureNotReferenced()

            buffer = sqliteOutOfMemoryCheck(block()) {
                "Failed to allocate request amount of memory"
            }
        }
    }

    /**
     * Resizes this buffer to [newSize]. The buffer is left untouched if allocation fails.
     *
     * @throws IllegalArgumentException if [newSize] is not positive.
     * @throws BufferInUseException if SQLite is currently borrowing this buffer, for example as a
     * bound statement parameter.
     * @throws ksqlite.kapi.SQLiteException if there is not enough memory available.
     */
    public fun resize(newSize: Int): Unit =
        resize(newSize.toLong()) { sqlite3_realloc(buffer, newSize) }

    /**
     * Resizes this buffer to [newSize]. The buffer is left untouched if allocation fails.
     *
     * @throws IllegalArgumentException if [newSize] is not positive.
     * @throws BufferInUseException if SQLite is currently borrowing this buffer, for example as a
     * bound statement parameter.
     * @throws ksqlite.kapi.SQLiteException if there is not enough memory available.
     */
    public fun resize(newSize: Long): Unit =
        resize(newSize) { sqlite3_realloc64(buffer, newSize) }

    /**
     * Releases the memory this buffer holds. Calling this again on an already closed buffer has no
     * effect.
     *
     * @throws BufferInUseException if SQLite is currently borrowing this buffer, for example as a
     * bound statement parameter. The buffer is left open in that case, and this method can be
     * called again once it is no longer borrowed.
     */
    override fun close() {
        if (!scope.closed) {
            ensureNotReferenced()
        }

        scope.close()
    }

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides factory functions for allocating a [Buffer].
     */
    public companion object {

        /**
         * Returns a [Buffer] wrapping this [CapiBuffer].
         */
        internal fun CapiBuffer.wrap(): Buffer = Buffer(this)

        /**
         * Returns a [Buffer] wrapping the [CapiBuffer] returned by [malloc].
         */
        private inline fun allocate(
            size: Long,
            malloc: () -> CapiBuffer?
        ): Buffer {
            require(size > 0L) { "Size must be greater than 0 ($size requested)" }

            return sqliteOutOfMemoryCheck(malloc()) { "Not enough memory to allocate $size bytes" }
                .wrap()
        }

        /**
         * Allocates a native memory region of [size] bytes and returns a [Buffer] for reading and
         * writing it.
         *
         * @throws IllegalArgumentException if [size] is not positive.
         * @throws ksqlite.kapi.SQLiteException if there is not enough memory available.
         */
        public fun allocate(size: Int): Buffer = allocate(size.toLong()) { sqlite3_malloc(size) }

        /**
         * Allocates a native memory region of [size] bytes and returns a [Buffer] for reading and
         * writing it.
         *
         * @throws IllegalArgumentException if [size] is not positive.
         * @throws ksqlite.kapi.SQLiteException if there is not enough memory available.
         */
        public fun allocate(size: Long): Buffer = allocate(size) { sqlite3_malloc64(size) }
    }
}