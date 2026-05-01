package ksqlite.capi.memory

import ksqlite.capi.utils.checkBufferRange
import java.lang.foreign.MemorySegment

/**
 * Implementation of both [ReadableMemoryBlock] and [WritableMemoryBlock] for JVM.
 */
internal class MemoryBlock(
    val pointer: MemorySegment,
    val blockSize: Long
) : ReadableMemoryBlock,
    WritableMemoryBlock {

    override fun read(
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int,
        destination: ByteArray
    ): ByteArray {
        checkBufferRange(
            sourceOffset = sourceOffset,
            sourceSize = blockSize,
            destinationOffset = destinationOffset.toLong(),
            destinationSize = destination.size.toLong(),
            size = size
        )

        MemorySegment.copy(
            pointer,
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
            destinationSize = blockSize,
            size = size
        )

        MemorySegment.copy(
            MemorySegment.ofArray(source),
            sourceOffset.toLong(),
            pointer,
            destinationOffset,
            size.toLong()
        )
    }
}