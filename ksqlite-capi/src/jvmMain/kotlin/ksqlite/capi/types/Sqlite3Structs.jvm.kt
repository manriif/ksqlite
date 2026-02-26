@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.MemoryPointer
import java.lang.foreign.MemorySegment

public actual class sqlite3(pointer: MemorySegment) : MemoryPointer(pointer)

public actual class sqlite3_context(pointer: MemorySegment) : MemoryPointer(pointer)

public actual class sqlite3_stmt(pointer: MemorySegment) : MemoryPointer(pointer)

public actual class sqlite3_value(pointer: MemorySegment) : MemoryPointer(pointer)
