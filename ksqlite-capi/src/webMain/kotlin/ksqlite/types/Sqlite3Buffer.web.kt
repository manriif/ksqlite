package ksqlite.types

public actual class Sqlite3Buffer(
    public actual val nativeSize: ULong
) {

    public actual fun read(
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int,
        destination: ByteArray
    ): ByteArray {
        TODO("Not yet implemented")
    }

    public actual fun write(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    ) {
    }
}