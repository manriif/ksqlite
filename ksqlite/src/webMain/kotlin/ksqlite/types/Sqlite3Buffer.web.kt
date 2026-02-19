package ksqlite.types

public actual class Sqlite3Buffer(
    public actual val nativeSize: Int
) {

    public actual fun read(
        sourceOffset: Int,
        destinationOffset: Int,
        size: Int,
        destination: ByteArray
    ): ByteArray {
        TODO("Not yet implemented")
    }

    public actual fun write(
        source: ByteArray,
        sourceOffset: Int,
        destinationOffset: Int,
        size: Int
    ) {
    }
}