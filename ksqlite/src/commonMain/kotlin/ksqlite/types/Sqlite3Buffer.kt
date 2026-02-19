package ksqlite.types

/**
 * Memory slot holding application data.
 */
public expect class Sqlite3Buffer {

    /**
     * Size of the buffer allocated by SQLite.
     */
    public val nativeSize: Int

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
        sourceOffset: Int = 0,
        destinationOffset: Int = 0,
        size: Int = this.nativeSize,
        destination: ByteArray = run {
            require(size >= 0) { "size must not be negative ($size)" }
            ByteArray(size)
        }
    ): ByteArray

    /**
     * Sets the native buffer content from [source], copying bytes from [start] (inclusive) to [end]
     * (exclusive).
     *
     * @throws IllegalArgumentException if any of [sourceOffset], [destinationOffset] or [size] is
     * negative.
     * @throws IndexOutOfBoundsException if not all bytes can fit in native buffer or can be read
     * from [source].
     */
    public fun write(
        source: ByteArray,
        sourceOffset: Int = 0,
        destinationOffset: Int = 0,
        size: Int = source.size,
    )
}