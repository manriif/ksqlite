@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.Pointer
import java.lang.foreign.MemorySegment

public actual class sqlite3(pointer: MemorySegment) : Pointer(pointer)

public actual class sqlite3_context(pointer: MemorySegment) : Pointer(pointer)

public actual class sqlite3_stmt(pointer: MemorySegment) : Pointer(pointer)

public actual class sqlite3_value(pointer: MemorySegment) : Pointer(pointer)
