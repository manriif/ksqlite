package ksqlite.capi.memory

import ksqlite.capi.wasm
import ksqlite.js.copyFrom
import ksqlite.js.copyTo
import ksqlite.js.plus
import ksqlite.wasm.WasmMemory
import ksqlite.wasm.WasmPointer
import kotlin.js.toLong

public actual open class Buffer internal constructor(
    internal val memory: WasmMemory,
    internal val pointer: WasmPointer,
    byteSize: Long
) : BufferBase(byteSize) {

    actual override val address: Long
        get() = pointer.toLong()

    actual override fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    ) {
        val begin = (pointer + sourceOffset).toLong().toInt()
        val end = begin + size

        memory.heap8()
            .subarray(begin, end)
            .copyTo(destination, destinationOffset)
    }

    actual override fun nativeWrite(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    ) {
        val begin = (pointer + destinationOffset).toLong().toInt()
        val end = begin + size

        memory.heap8()
            .subarray(begin, end)
            .copyFrom(source, sourceOffset)
    }

    internal actual companion object {

        actual val Empty = Buffer(wasm, NullPtr, 0)

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(
            pointer: WasmPointer,
            size: Long,
            memory: WasmMemory = wasm
        ): Buffer? = pointer.orNull?.let {
            Buffer(memory, pointer, size)
        }
    }
}