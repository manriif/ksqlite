@file:Suppress("ClassName")

package ksqlite.types

import ksqlite.memory.Pointer
import ksqlite.memory.isNull
import java.lang.foreign.MemorySegment

public actual class sqlite3_pointer private constructor(
    internal val segment: MemorySegment,
    internal val size: Long
) {
    public companion object {
        internal fun from(segment: MemorySegment, size: Long): sqlite3_pointer? {
            if (segment.isNull) {
                return null
            }

            return sqlite3_pointer(segment.reinterpret(size), size)
        }
    }
}

public actual class sqlite3 : Pointer()

public actual class sqlite3_context : Pointer()

public actual class sqlite3_stmt : Pointer()
