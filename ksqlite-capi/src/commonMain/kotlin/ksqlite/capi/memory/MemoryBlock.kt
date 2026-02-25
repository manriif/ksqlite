package ksqlite.capi.memory

/**
 * Block of memory which can be read.
 */
public interface ReadableMemoryBlock {

    /**
     * Reads [size] bytes from the memory block into [destination] and returns [destination].
     * Reading starts at [sourceOffset] from the memory block and writing starts at
     * [destinationOffset] to the [destination] buffer.
     *
     * @throws IllegalArgumentException if any of [sourceOffset], [destinationOffset] or [size] is
     * negative.
     * @throws IndexOutOfBoundsException if not all bytes can fit in [destination] or can be read
     * from memory block.
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
 * Block of memory which can be written.
 */
public interface WritableMemoryBlock {

    /**
     * Writes [size] bytes from [source] to the memory block.
     * Reading starts at [sourceOffset] from the [source] buffer and writing starts at
     * [destinationOffset] to the memory block.
     *
     * @throws IllegalArgumentException if any of [sourceOffset], [destinationOffset] or [size] is
     * negative.
     * @throws IndexOutOfBoundsException if not all bytes can fit in the memory block or can be read
     * from [source].
     */
    public fun write(
        source: ByteArray,
        size: Int = source.size,
        sourceOffset: Int = 0,
        destinationOffset: Long = 0,
    )
}