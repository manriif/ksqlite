package ksqlite.capi.memory

/**
 * Block of memory which can be read.
 */
public interface ReadableMemoryBlock {

    /**
     * Reads [size] bytes from the native memory block into [destination].
     *
     * The read starts at [sourceOffset] in the native memory region and writes into [destination]
     * starting at [destinationOffset].
     *
     * @return the same [destination] array for convenience
     *
     * @throws IllegalArgumentException if [size], [sourceOffset], or [destinationOffset] is
     * negative
     * @throws IndexOutOfBoundsException if the requested range is out of bounds in either the
     * native memory block or [destination]
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
        destinationOffset: Long = 0,
    )
}