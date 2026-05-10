package ksqlite.capi.memory

import ksqlite.capi.interop.js.arraySize
import ksqlite.capi.interop.js.copyFrom
import ksqlite.capi.interop.js.copyTo
import ksqlite.capi.interop.js.plus
import ksqlite.capi.interop.wasm.WasmMemory
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.utils.checkBufferRange
import kotlin.js.toLong

/**
 * Implementation of both [ReadableMemoryBlock] and [WritableMemoryBlock] for JVM.
 */
internal class MemoryBlock(
    val memory: WasmMemory,
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
            destinationSize = arraySize(destination).toLong(),
            size = size
        )

        val begin = (pointer + sourceOffset).toLong().toInt()
        val end = begin + size

        memory.heap8()
            .subarray(begin, end)
            .copyTo(destination, destinationOffset)

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
            sourceSize = arraySize(source).toLong(),
            destinationOffset = destinationOffset,
            destinationSize = blockSize,
            size = size
        )

        val begin = (pointer + destinationOffset).toLong().toInt()
        val end = begin + size

        memory.heap8()
            .subarray(begin, end)
            .copyFrom(source, sourceOffset)
    }
}