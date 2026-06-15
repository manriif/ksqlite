package ksqlite.kapi.buffer

import ksqlite.capi.memory.read
import ksqlite.capi.memory.readBytes
import ksqlite.capi.memory.readBytesOrThrow
import ksqlite.capi.memory.ReadableBuffer as CapiReadableBuffer

/**
 * Region of native memory that can be read.
 */
public open class ReadableBuffer internal constructor(internal open val buffer: CapiReadableBuffer) {

    /**
     * Size of the memory region in bytes.
     */
    public val byteSize: Long
        get() = buffer.byteSize

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
    ): Unit = buffer.read(
        destination = destination,
        size = size,
        sourceOffset = sourceOffset,
        destinationOffset = destinationOffset
    )
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Reads [size] bytes from the native memory block and returns a [ByteArray] holding them.
 *
 * The read starts at [sourceOffset] in the native memory region and writes into the returned
 * [ByteArray] starting at [destinationOffset].
 *
 * @throws IllegalArgumentException if [size], [sourceOffset], or [destinationOffset] is
 * negative
 * @throws IndexOutOfBoundsException if the requested range is out of bounds in either the
 * native memory block or the returned [ByteArray]
 */
public fun ReadableBuffer.read(
    size: Int,
    sourceOffset: Long = 0,
    destinationOffset: Int = 0,
): ByteArray = buffer.read(
    size = size,
    sourceOffset = sourceOffset,
    destinationOffset = destinationOffset
)

/**
 * Reads at most [Int.MAX_VALUE] bytes from `this` buffer.
 */
public fun ReadableBuffer.readBytes(): ByteArray =
    buffer.readBytes()

/**
 * Reads all bytes from `this` buffer.
 *
 * @throws UnsupportedOperationException if not all bytes in `this` buffer can fit into a
 * [ByteArray].
 */
public fun ReadableBuffer.readBytesOrThrow(): ByteArray =
    buffer.readBytesOrThrow()