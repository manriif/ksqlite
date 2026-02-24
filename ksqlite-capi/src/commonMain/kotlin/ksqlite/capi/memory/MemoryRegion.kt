package ksqlite.capi.memory

/**
 * Region of memory which can be read.
 */
public interface ReadableMemoryRegion {

    /**
     * Reads [size] bytes from the native buffer into [destination] and returns [destination].
     * Reading starts at [sourceOffset] from the native buffer and writing starts at
     * [destinationOffset] to the [destination] buffer.
     *
     * @throws IllegalArgumentException if any of [sourceOffset], [destinationOffset] or [size] is
     * negative.
     * @throws IndexOutOfBoundsException if not all bytes can fit in [destination] or can be read
     * from native buffer.
     */
    public fun read(
        size: Int,
        sourceOffset: Long = 0,
        destinationOffset: Int = 0,
        destination: ByteArray = run {
            require(size >= 0) { "size must not be negative ($size)" }
            ByteArray(size)
        }
    ): ByteArray
}

/**
 * Region of memory which can be written.
 */
public interface WritableMemoryRegion {

    /**
     * Writes [size] bytes from [source] to the native buffer.
     * Reading starts at [sourceOffset] from the [source] buffer and writing starts at
     * [destinationOffset] to the native buffer.
     *
     * @throws IllegalArgumentException if any of [sourceOffset], [destinationOffset] or [size] is
     * negative.
     * @throws IndexOutOfBoundsException if not all bytes can fit in native buffer or can be read
     * from [source].
     */
    public fun write(
        source: ByteArray,
        size: Int = source.size,
        sourceOffset: Int = 0,
        destinationOffset: Long = 0,
    )
}