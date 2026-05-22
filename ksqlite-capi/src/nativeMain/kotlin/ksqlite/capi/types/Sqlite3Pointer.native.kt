@file:Suppress("ClassName")

package ksqlite.capi.types

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.reinterpret
import ksqlite.capi.memory.MemoryBlock
import ksqlite.capi.memory.ReadableMemoryBlock
import ksqlite.capi.memory.WritableMemoryBlock
import ksqlite.capi.memory.disposeStableRef
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.sqlite3_pointer
import ksqlite.capi.memory.BufferBase

public actual open class sqlite3_pointer internal constructor(internal val block: MemoryBlock) :
    BufferBase(),
    ReadableMemoryBlock by block {

    actual override val address: Long
        get() = block.pointer.rawValue.toLong()

    public actual override val byteSize: Long
        get() = block.blockSize

    internal companion object {

        /**
         * Returns a [ksqlite.capi.memory.sqlite3_pointer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: COpaquePointer?, size: Long): sqlite3_pointer? = pointer?.let {
            ksqlite.capi.memory.sqlite3_pointer(MemoryBlock(pointer.reinterpret(), size))
        }
    }
}

public actual class sqlite3_mutable_pointer internal constructor(block: MemoryBlock) :
    sqlite3_pointer(block),
    WritableMemoryBlock by block {

    internal companion object {

        /**
         * Returns a [ksqlite.capi.memory.Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: COpaquePointer?, size: Long): Buffer? = pointer?.let {
            ksqlite.capi.memory.Buffer(MemoryBlock(pointer.reinterpret(), size))
        }

        /**
         * Returns a [ksqlite.capi.memory.Buffer] from [pointer] or `null` if [pointer] is `null`.
         * The returned [ksqlite.capi.memory.Buffer] is obtained from [disposeStableRef].
         */
        fun fromStableRef(pointer: COpaquePointer?): Buffer? = pointer?.let {
            disposeStableRef(pointer)
        }
    }
}