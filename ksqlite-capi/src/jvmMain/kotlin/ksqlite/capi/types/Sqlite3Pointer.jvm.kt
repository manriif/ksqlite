@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.MemoryBlock
import ksqlite.capi.memory.ReadableMemoryBlock
import ksqlite.capi.memory.WritableMemoryBlock
import ksqlite.capi.memory.isNull
import java.lang.foreign.MemorySegment

public actual open class sqlite3_pointer internal constructor(
    private val region: MemoryBlock
) : ReadableMemoryBlock by region {

    public actual val size: Long
        get() = region.blockSize

    internal companion object {

        fun from(segment: MemorySegment, size: Long): sqlite3_pointer? {
            if (segment.isNull) {
                return null
            }

            return sqlite3_pointer(MemoryBlock(segment, size))
        }
    }
}

public actual class sqlite3_mutable_pointer internal constructor(region: MemoryBlock) :
    sqlite3_pointer(region),
    WritableMemoryBlock by region {

    internal companion object {

        fun from(segment: MemorySegment, size: Long): sqlite3_mutable_pointer? {
            if (segment.isNull) {
                return null
            }

            return sqlite3_mutable_pointer(MemoryBlock(segment, size))
        }
    }
}