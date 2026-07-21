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

/**
 * Region of native memory that can be read.
 */
public abstract class ReadableBuffer internal constructor(
    /**
     * Size of the memory region in bytes.
     */
    public val byteSize: Long
) {

    /**
     * Reads from native memory.
     */
    internal abstract fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    )

    /**
     * Reads [size] bytes from the native memory block into [destination].
     *
     * The read starts at [sourceOffset] in the native memory region and writes into [destination]
     * starting at [destinationOffset].
     *
     * @throws IllegalArgumentException if [size], [sourceOffset], or [destinationOffset] is
     * negative
     * @throws IndexOutOfBoundsException if the requested range is out of bounds in either the
     * native memory block or [destination]
     */
    public fun read(
        destination: ByteArray,
        size: Int = destination.size,
        sourceOffset: Long = 0,
        destinationOffset: Int = 0
    ) {
        checkBufferRange(
            sourceOffset = sourceOffset,
            sourceSize = byteSize,
            destinationOffset = destinationOffset.toLong(),
            destinationSize = destination.size.toLong(),
            size = size
        )

        nativeRead(
            destination = destination,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }
}

/**
 * Region of native memory that can be written.
 */
public abstract class WritableBuffer internal constructor(byteSize: Long) :
    ReadableBuffer(byteSize) {

    /**
     * Writes to native memory.
     */
    protected abstract fun nativeWrite(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    )

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
        size: Int = source.size,
        sourceOffset: Int = 0,
        destinationOffset: Long = 0
    ) {
        checkBufferRange(
            sourceOffset = sourceOffset.toLong(),
            sourceSize = source.size.toLong(),
            destinationOffset = destinationOffset,
            destinationSize = byteSize,
            size = size
        )

        nativeWrite(
            source = source,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }
}

/**
 * Wrapper around platform buffer that only allows reading.
 */
private class ReadOnlyBuffer(private val source: ReadableBuffer) :
    ReadableBuffer(source.byteSize) {

    override fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    ) {
        source.nativeRead(
            destination = destination,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }

    override fun toString(): String = source.toString()
}

/**
 * Base class for [Buffer] implementations.
 */
public abstract class BufferBase internal constructor(byteSize: Long) : WritableBuffer(byteSize) {

    /**
     * Native address of the first byte.
     */
    internal abstract val address: Long

    override fun toString(): String =
        "Buffer(address=0x${address.toHexString(NativeAddressHexFormat)}, size=$byteSize)"

    /**
     * Returns a readonly view of this [Buffer].
     */
    internal fun readOnly(): ReadableBuffer = ReadOnlyBuffer(this)
}

/**
 * SQLite managed memory region that can be read and written.
 *
 * The memory region can theoretically carry up to [Long.MAX_VALUE] bytes on JVM (+ Android) and
 * Native, but it can be limited to [Int.MAX_VALUE] on web targets.
 *
 * The memory is allocated using [ksqlite.capi.sqlite3_malloc], [ksqlite.capi.sqlite3_malloc64],
 * [ksqlite.capi.sqlite3_realloc] or [ksqlite.capi.sqlite3_realloc64] and must be freed when no
 * longer required by passing `this` buffer to [ksqlite.capi.sqlite3_free].
 *
 * [Buffer] does not provide any kind of thread-safety and external synchronization is required if
 * concurrent access is needed.
 */
public expect class Buffer : BufferBase {

    override val address: Long

    override fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    )

    override fun nativeWrite(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    )

    internal companion object {

        /**
         * Empty buffer ([byteSize] == 0).
         */
        val Empty: Buffer
    }
}

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Ensures that [sourceOffset], [destinationOffset] and [size] are not negative and that content can
 * fit in source and destination.
 */
@Suppress("NOTHING_TO_INLINE")
private inline fun checkBufferRange(
    sourceOffset: Long,
    sourceSize: Long,
    destinationOffset: Long,
    destinationSize: Long,
    size: Int
) {
    require(sourceOffset >= 0) {
        "sourceOffset must not be negative ($sourceOffset)"
    }

    require(destinationOffset >= 0) {
        "destinationOffset must not be negative ($destinationOffset)"
    }

    require(size >= 0) {
        "size must not be negative ($size)"
    }

    require((sourceSize - sourceOffset) >= size) {
        "source buffer cannot provides the requested number of bytes"
    }

    require((destinationSize - destinationOffset) >= size) {
        "destination buffer cannot receives the requested number of bytes"
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Reads [size] bytes from the native memory block and returns a [ByteArray] holding them.
 *
 * The read starts at [offset] in the native memory region and writes into the returned [ByteArray].
 *
 * @throws IllegalArgumentException if [size] or [offset] is negative
 * @throws IndexOutOfBoundsException if the requested range is out of bounds in either the
 * native memory block or the returned [ByteArray]
 */
public fun ReadableBuffer.read(
    size: Int,
    offset: Long = 0
): ByteArray {
    require(size >= 0) { "size must not be negative ($size)" }
    require(offset >= 0) { "offset must not be negative ($offset)" }

    val destination = ByteArray(size)

    read(
        destination = destination,
        size = size,
        sourceOffset = offset,
        destinationOffset = 0
    )

    return destination
}

/**
 * Reads at most [Int.MAX_VALUE] bytes from `this` buffer.
 */
public fun ReadableBuffer.readBytes(): ByteArray {
    val size = byteSize
        .coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong())
        .toInt()

    return read(size)
}

/**
 * Reads all bytes from `this` buffer.
 *
 * @throws UnsupportedOperationException if not all bytes in `this` buffer can fit into a
 * [ByteArray].
 */
public fun ReadableBuffer.readBytesOrThrow(): ByteArray {
    if (byteSize > Int.MAX_VALUE.toLong()) {
        throw UnsupportedOperationException(
            "Buffer size exceeds the maximum value that can fit into a 4 bytes signed integer " +
                    "($byteSize > ${Int.MAX_VALUE})"
        )
    }

    return read(byteSize.toInt())
}