package ksqlite.capi.memory

import ksqlite.capi.utils.checkBufferRange
import ksqlite.nativeBufferRead
import ksqlite.nativeBufferWrite
import ksqlite.requireBuffer

/**
 * Implementation of both [ReadableMemoryBlock] and [WritableMemoryBlock] for Android.
 */
internal class MemoryBlock(
    val pointer: Long,
    val blockSize: Long
) : ReadableMemoryBlock,
    WritableMemoryBlock {

    val buffer by lazy {
        requireBuffer(pointer, blockSize)
    }

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

        nativeBufferRead(
            buffer = buffer,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset,
            destination = destination
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

        nativeBufferWrite(
            buffer = buffer,
            source = source,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }
}