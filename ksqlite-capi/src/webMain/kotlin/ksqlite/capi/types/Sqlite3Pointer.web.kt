@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.interop.wasm.WasmMemory
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.memory.MemoryBlock
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.ReadableMemoryBlock
import ksqlite.capi.memory.WritableMemoryBlock
import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.sqlite3_pointer
import ksqlite.capi.memory.BufferBase
import ksqlite.capi.wasm
import kotlin.js.toLong

public actual open class sqlite3_pointer internal constructor(internal val block: MemoryBlock) :
    BufferBase(),
    ReadableMemoryBlock by block {

    actual override val address: Long
        get() = block.pointer.toLong()

    public actual override val byteSize: Long
        get() = block.blockSize

    internal companion object {

        /**
         * Returns a [ksqlite.capi.memory.sqlite3_pointer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(
            pointer: WasmPointer,
            size: Long,
            memory: WasmMemory = wasm
        ): sqlite3_pointer? {
            if (pointer.isNull) {
                return null
            }

            return ksqlite.capi.memory.sqlite3_pointer(MemoryBlock(memory, pointer, size))
        }
    }
}

public actual class sqlite3_mutable_pointer internal constructor(region: MemoryBlock) :
    sqlite3_pointer(region),
    WritableMemoryBlock by region {

    internal companion object {

        /**
         * Returns a [sqlite3_pointer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(
            pointer: WasmPointer,
            size: Long,
            memory: WasmMemory = wasm
        ): Buffer? {
            if (pointer.isNull) {
                return null
            }

            return ksqlite.capi.memory.Buffer(MemoryBlock(memory, pointer, size))
        }

        /**
         * Returns a [ksqlite.capi.memory.Buffer] from [pointer] or `null` if [pointer] is `null`.
         * The returned [ksqlite.capi.memory.Buffer] is obtained from [MemoryManager.getStableRef].
         *
         * The reference is disposed before being returned if [dispose] is `true`.
         */
        context(manager: MemoryManager)
        fun fromStableRef(
            pointer: WasmPointer,
            dispose: Boolean = true
        ): Buffer? {
            if (pointer.isNull) {
                return null
            }

            return manager.getStableRef(pointer).run {
                userData?.also {
                    if (dispose) {
                        dispose()
                    }
                }
            }
        }
    }
}