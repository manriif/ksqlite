package ksqlite.capi.memory

import ksqlite.capi.memory.ReadableMemoryRegion
import ksqlite.capi.memory.WritableMemoryRegion
import ksqlite.capi.utils.checkBufferRange
import java.lang.foreign.MemorySegment

internal class MemoryRegion(
    private val nativeSegment: MemorySegment,
    val nativeSize: Long
) : ReadableMemoryRegion,
    WritableMemoryRegion {

    override fun read(
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

    override fun write(
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