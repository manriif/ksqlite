package ksqlite.capi.memory

import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.utils.checkBufferRange

/**
 * Implementation of both [ReadableMemoryBlock] and [WritableMemoryBlock] for JVM.
 */
internal class MemoryBlock(
    val pointer: WasmPointer,
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

        TODO()

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

        TODO()
    }
}