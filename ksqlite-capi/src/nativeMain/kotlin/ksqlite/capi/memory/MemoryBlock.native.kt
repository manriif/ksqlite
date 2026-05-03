package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.plus
import kotlinx.cinterop.usePinned
import ksqlite.capi.utils.checkBufferRange
import platform.posix.memcpy

/**
 * Implementation of both [ReadableMemoryBlock] and [WritableMemoryBlock] for Native.
 */
internal class MemoryBlock(
    val pointer: CPointer<ByteVar>,
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

        destination.usePinned { pinned ->
            val _ = memcpy(
                __dst = pinned.addressOf(destinationOffset),
                __src = pointer + sourceOffset,
                __n = size.convert()
            )
        }

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

        source.usePinned { pinned ->
            val _ = memcpy(
                __dst = pointer + destinationOffset,
                __src = pinned.addressOf(sourceOffset),
                __n = size.convert()
            )
        }
    }
}