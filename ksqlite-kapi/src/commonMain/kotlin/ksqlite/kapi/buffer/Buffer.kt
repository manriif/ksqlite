package ksqlite.kapi.buffer

import ksqlite.capi.sqlite3_free
import ksqlite.capi.sqlite3_malloc
import ksqlite.capi.sqlite3_malloc64
import ksqlite.kapi.helpers.sqliteOutOfMemoryCheck
import ksqlite.capi.memory.Buffer as CapiBuffer

/**
 * Region of native memory that can be read and written.
 *
 * [Buffer] does not provide any kind of thread-safety and external synchronization is required if
 * concurrent access is needed.
 *
 * The [Buffer] must be closed once no longer needed to release allocated resources.
 */
public class Buffer internal constructor(override val buffer: CapiBuffer) :
    ReadableBuffer(buffer),
    AutoCloseable {

    /**
     * Writes [size] bytes from [source] into the native memory block.
     *
     * Reading starts at [sourceOffset] in [source], and writing starts at [destinationOffset] in
     * the native memory region.
     *
     * @throws IllegalArgumentException if [size], [sourceOffset], or [destinationOffset] is
     * negative
     * @throws IndexOutOfBoundsException if the requested range is out of bounds in either [source]
     * or the native memory block
     */
    public fun write(
        source: ByteArray,
        size: Int,
        sourceOffset: Int = 0,
        destinationOffset: Long = 0
    ): Unit = buffer.write(
        source = source,
        size = size,
        sourceOffset = sourceOffset,
        destinationOffset = destinationOffset
    )

    /**
     * Resizes this buffer to [newSize].
     */
    public fun resize(newSize: Int): Buffer {

    }

    /**
     * Resizes this buffer to [newSize].
     */
    public fun resize(newSize: Long): Buffer {

    }

    /**
     * Releases allocated resources.
     */
    override fun close() {
        sqlite3_free(buffer)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Companion
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Provides factory functions for allocating a [Buffer].
     */
    public companion object {

        /**
         * Allocates a native memory region of [size] bytes and returns a [Buffer] that allows read
         * and write operations on that region.
         */
        public fun allocate(size: Int): Buffer =
            Buffer(sqliteOutOfMemoryCheck(sqlite3_malloc(size)) {
                "Not enough memory to allocate $size bytes"
            })

        /**
         * Allocates a native memory region of [size] bytes and returns a [Buffer] that allows read
         * and write operations on that region.
         */
        public fun allocate(size: Long): Buffer =
            Buffer(sqliteOutOfMemoryCheck(sqlite3_malloc64(size)) {
                "Not enough memory to allocate $size bytes"
            })
    }
}