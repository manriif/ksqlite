package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.get
import kotlinx.cinterop.set
import ksqlite.capi.memory.ReadableMemoryRegion
import ksqlite.capi.memory.WritableMemoryRegion
import ksqlite.capi.utils.checkBufferRange

/**
 * Implementation of both [ReadableMemoryRegion] and [WritableMemoryRegion] for native.
 */
internal class MemoryRegion(
    private val nativeBuffer: CPointer<ByteVar>,
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

        repeat(size) { index ->
            destination[destinationOffset + index] = nativeBuffer[sourceOffset + index]
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
            destinationSize = nativeSize,
            size = size
        )

        repeat(size) { index ->
            nativeBuffer[destinationOffset + index] = source[sourceOffset + index]
        }
    }
}