package ksqlite.types

import ksqlite.utils.checkBufferRange
import java.lang.foreign.MemorySegment

public actual class Sqlite3Buffer(
    private val nativeSegment: MemorySegment,
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

        MemorySegment.copy(
            nativeSegment,
            sourceOffset,
            MemorySegment.ofArray(destination),
            destinationOffset.toLong(),
            size.toLong()
        )

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

        MemorySegment.copy(
            MemorySegment.ofArray(source),
            sourceOffset.toLong(),
            nativeSegment,
            destinationOffset,
            size.toLong()
        )
    }
}