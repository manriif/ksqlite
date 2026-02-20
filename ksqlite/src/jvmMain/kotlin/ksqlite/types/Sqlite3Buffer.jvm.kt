package ksqlite.types

import ksqlite.utils.checkRange
import ksqlite.memory.isNull
import java.lang.foreign.MemorySegment

public actual class Sqlite3Buffer(
    private val nativeSegment: MemorySegment,
    public actual val nativeSize: Int
) {

    public actual fun read(
        sourceOffset: Int,
        destinationOffset: Int,
        size: Int,
        destination: ByteArray
    ): ByteArray {
        checkRange(
            sourceOffset = sourceOffset,
            sourceSize = nativeSize,
            destinationOffset = destinationOffset,
            destinationSize = destination.size,
            size = size
        )

        MemorySegment.copy(
            nativeSegment,
            sourceOffset.toLong(),
            MemorySegment.ofArray(destination),
            destinationOffset.toLong(),
            size.toLong()
        )

        return destination
    }

    public actual fun write(
        source: ByteArray,
        sourceOffset: Int,
        destinationOffset: Int,
        size: Int
    ) {
        checkRange(
            sourceOffset = sourceOffset,
            sourceSize = source.size,
            destinationOffset = destinationOffset,
            destinationSize = nativeSize,
            size = size
        )

        MemorySegment.copy(
            MemorySegment.ofArray(source),
            sourceOffset.toLong(),
            nativeSegment,
            destinationOffset.toLong(),
            size.toLong()
        )
    }
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an [Sqlite3Buffer] or `null` if [sqlite3_pointer] is `null`.
 */
internal fun createBuffer(segment: MemorySegment, size: Int): Sqlite3Buffer? {
    if (segment.isNull) {
        return null
    }

    return Sqlite3Buffer(
        nativeSegment = segment,
        nativeSize = size
    )
}