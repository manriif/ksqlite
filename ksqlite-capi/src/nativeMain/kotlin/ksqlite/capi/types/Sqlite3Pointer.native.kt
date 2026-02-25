@file:Suppress("ClassName")

package ksqlite.capi.types

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.reinterpret
import ksqlite.capi.memory.MemoryBlock
import ksqlite.capi.memory.ReadableMemoryBlock
import ksqlite.capi.memory.WritableMemoryBlock

public actual open class sqlite3_pointer internal constructor(internal val block: MemoryBlock) :
    ReadableMemoryBlock by block {

    public actual val size: Long
        get() = block.blockSize

    internal companion object {

        fun from(pointer: COpaquePointer?, size: Long) = pointer?.let { pointer ->
            sqlite3_pointer(MemoryBlock(pointer.reinterpret(), size))
        }
    }
}

public actual class sqlite3_mutable_pointer internal constructor(block: MemoryBlock) :
    sqlite3_pointer(block),
    WritableMemoryBlock by block {

    internal companion object {

        fun from(pointer: COpaquePointer?, size: Long) = pointer?.let { pointer ->
            sqlite3_mutable_pointer(MemoryBlock(pointer.reinterpret(), size))
        }
    }
}