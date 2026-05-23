package ksqlite.capi.memory

/**
 * Provides common code to all platform for [Buffer].
 *
 * The memory region can theoretically carry up to [ULong.MAX_VALUE] bytes on JVM and Native, but
 * it can be limited to [UInt.MAX_VALUE] on web targets.
 *
 * The native memory is managed by SQLite and must be freed when no longer required by passing
 * `this` buffer to [ksqlite.capi.sqlite3_free].
 *
 * [Buffer] does not provide any kind of thread-safety and external synchronization is required.
 */
public abstract class BufferBase internal constructor(
    /**
     * Size of the memory region in bytes.
     */
    public val byteSize: Long
) {

    /**
     * Native address of the first byte.
     */
    protected abstract val address: Long

    /**
     * Reads from native memory.
     */
    protected abstract fun nativeRead(
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
        size: Int,
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

    override fun toString(): String {
        return "Buffer(address=0x${address.toHexString()}, size=$byteSize)"
    }
}

/**
 * Platform [BufferBase] implementation.
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
 * The read starts at [sourceOffset] in the native memory region and writes into the returned
 * [ByteArray] starting at [destinationOffset].
 *
 * @throws IllegalArgumentException if [size], [sourceOffset], or [destinationOffset] is
 * negative
 * @throws IndexOutOfBoundsException if the requested range is out of bounds in either the
 * native memory block or the returned [ByteArray]
 */
public fun Buffer.read(
    size: Int,
    sourceOffset: Long = 0,
    destinationOffset: Int = 0,
): ByteArray {
    require(size >= 0) { "size must not be negative ($size)" }
    val destination = ByteArray(size)

    read(
        destination = destination,
        size = size,
        sourceOffset = sourceOffset,
        destinationOffset = destinationOffset
    )

    return destination
}

/**
 * Reads at most [Int.MAX_VALUE] bytes from `this` buffer.
 */
public fun Buffer.readBytes(): ByteArray {
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
public fun Buffer.readBytesOrThrow(): ByteArray {
    if (byteSize > Int.MAX_VALUE.toLong()) {
        throw UnsupportedOperationException(
            "Buffer size exceeds the maximum value that can fit into a 4 bytes signed integer " +
                    "($byteSize > ${Int.MAX_VALUE})"
        )
    }

    return read(byteSize.toInt())
}