package ksqlite.types

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import ksqlite.utils.checkBufferRange

public actual class Sqlite3Buffer(
    private val nativeBuffer: CPointer<ByteVar>,
    public actual val nativeSize: Long
) {

    public actual fun read(
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int,
        destination: ByteArray
    ): ByteArray {
        checkBufferRange(
            sourceOffset = sourceOffset,
            sourceSize = nativeSize,
            destinationOffset = destinationOffset.toLong(),
            destinationSize = destination.size.toLong(),
            size = size
        )

        repeat(size) { index ->
            destination[destinationOffset + index] = nativeBuffer[sourceOffset + index]
        }

        return destination
    }

    public actual fun write(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    ) {
        checkBufferRange(
            sourceOffset = sourceOffset.toLong(),
            sourceSize = source.size.toLong(),
            destinationOffset = destinationOffset,
            destinationSize = nativeSize,
            size = size
        )

        repeat(size) { index ->
            nativeBuffer[destinationOffset + index] = source[sourceOffset + index]
        }
    }
}