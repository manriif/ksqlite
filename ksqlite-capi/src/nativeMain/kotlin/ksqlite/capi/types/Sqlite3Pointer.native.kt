@file:Suppress("ClassName")

package ksqlite.capi.types

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.reinterpret
import ksqlite.capi.memory.MemoryRegion
import ksqlite.capi.memory.ReadableMemoryRegion
import ksqlite.capi.memory.WritableMemoryRegion

public actual open class sqlite3_pointer internal constructor(
    private val region: MemoryRegion
) : ReadableMemoryRegion by region {

    public actual val size: Long
        get() = region.nativeSize

    internal companion object {

        fun from(pointer: COpaquePointer?, size: Long) = pointer?.let { pointer ->
            sqlite3_pointer(MemoryRegion(pointer.reinterpret(), size))
        }
    }
}

public actual class sqlite3_mutable_pointer internal constructor(region: MemoryRegion) :
    sqlite3_pointer(region),
    WritableMemoryRegion by region {

    internal companion object {

        fun from(pointer: COpaquePointer?, size: Long) = pointer?.let { pointer ->
            sqlite3_mutable_pointer(MemoryRegion(pointer.reinterpret(), size))
        }
    }
}