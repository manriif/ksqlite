@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.MemoryRegion
import ksqlite.capi.memory.ReadableMemoryRegion
import ksqlite.capi.memory.WritableMemoryRegion
import ksqlite.capi.memory.isNull
import java.lang.foreign.MemorySegment

public actual open class sqlite3_pointer internal constructor(
    private val region: MemoryRegion
) : ReadableMemoryRegion by region {

    public actual val size: Long
        get() = region.nativeSize

    internal companion object {

        fun from(segment: MemorySegment, size: Long): sqlite3_pointer? {
            if (segment.isNull) {
                return null
            }

            return sqlite3_pointer(MemoryRegion(segment, size))
        }
    }
}

public actual class sqlite3_mutable_pointer internal constructor(region: MemoryRegion) :
    sqlite3_pointer(region),
    WritableMemoryRegion by region {

    internal companion object {

        fun from(segment: MemorySegment, size: Long): sqlite3_mutable_pointer? {
            if (segment.isNull) {
                return null
            }

            return sqlite3_mutable_pointer(MemoryRegion(segment, size))
        }
    }
}