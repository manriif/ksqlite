@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.MemoryBlock
import ksqlite.capi.memory.ReadableMemoryBlock
import ksqlite.capi.memory.WritableMemoryBlock
import ksqlite.capi.memory.isNullPointer

public actual open class sqlite3_pointer internal constructor(internal val block: MemoryBlock) :
    sqlite3_pointer_base(),
    ReadableMemoryBlock by block {

    actual override val address: Long
        get() = block.pointer

    public actual override val size: Long
        get() = block.blockSize

    internal companion object {

        /**
         * Returns a [sqlite3_pointer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: Long, size: Long): sqlite3_pointer? {
            if (pointer.isNullPointer) {
                return null
            }

            return sqlite3_pointer(MemoryBlock(pointer, size))
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
        fun from(pointer: Long, size: Long): sqlite3_mutable_pointer? {
            if (pointer.isNullPointer) {
                return null
            }

            return sqlite3_mutable_pointer(MemoryBlock(pointer, size))
        }
    }
}