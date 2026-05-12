@file:Suppress("ClassName")

package ksqlite.capi.types

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.reinterpret
import ksqlite.capi.memory.MemoryBlock
import ksqlite.capi.memory.ReadableMemoryBlock
import ksqlite.capi.memory.WritableMemoryBlock
import ksqlite.capi.memory.disposeStableRef

public actual open class sqlite3_pointer internal constructor(internal val block: MemoryBlock) :
    sqlite3_pointer_base(),
    ReadableMemoryBlock by block {

    actual override val address: Long
        get() = block.pointer.rawValue.toLong()

    public actual override val size: Long
        get() = block.blockSize

    internal companion object {

        /**
         * Returns a [sqlite3_pointer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: COpaquePointer?, size: Long): sqlite3_pointer? = pointer?.let {
            sqlite3_pointer(MemoryBlock(pointer.reinterpret(), size))
        }
    }
}

public actual class sqlite3_mutable_pointer internal constructor(block: MemoryBlock) :
    sqlite3_pointer(block),
    WritableMemoryBlock by block {

    internal companion object {

        /**
         * Returns a [sqlite3_mutable_pointer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: COpaquePointer?, size: Long): sqlite3_mutable_pointer? = pointer?.let {
            sqlite3_mutable_pointer(MemoryBlock(pointer.reinterpret(), size))
        }

        /**
         * Returns a [sqlite3_mutable_pointer] from [pointer] or `null` if [pointer] is `null`.
         * The returned [sqlite3_mutable_pointer] is obtained from [disposeStableRef].
         */
        fun fromStableRef(pointer: COpaquePointer?): sqlite3_mutable_pointer? = pointer?.let {
            disposeStableRef(pointer)
        }
    }
}