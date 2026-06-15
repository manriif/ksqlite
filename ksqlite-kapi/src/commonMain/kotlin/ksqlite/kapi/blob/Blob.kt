package ksqlite.kapi.blob

/**
 * Exposes the [BLOB API](https://sqlite.org/c3ref/blob.html).
 */
public interface Blob : AutoCloseable {

    /**
     * Returns the size of `this` blob in bytes.
     */
    public val bytes: Int

    /**
     * Reads [size] bytes from `this` blob, starting at [offset], into [output].
     *
     * @throws ksqlite.kapi.SQLiteException if the read operation fails.
     */
    public fun read(
        output: ByteArray,
        size: Int = output.size,
        offset: Int = 0
    )

    /**
     * Writes [size] bytes from [input] into `this` blob, starting at [offset].
     *
     * @throws ksqlite.kapi.SQLiteException if the write operation fails.
     */
    public fun write(
        input: ByteArray,
        size: Int = input.size,
        offset: Int = 0
    )

    /**
     * Moves `this` blob to the row identified by [rowid] of the same database, table and column.
     *
     * @throws ksqlite.kapi.SQLiteException if the move operation fails.
     */
    public fun reopen(rowid: Long)

    /**
     * Closes `this` open [Blob] unconditionally.
     *
     * @throws ksqlite.kapi.SQLiteException if closing the blob fails.
     */
    override fun close()
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Reads [size] bytes from the blob, starting at [offset] and returns the bytes read.
 */
public fun Blob.read(size: Int = bytes, offset: Int = 0): ByteArray = ByteArray(size).apply {
    read(this, size, offset)
}