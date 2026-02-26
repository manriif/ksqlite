@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.GenericPointer
import java.lang.foreign.MemorySegment

public actual class sqlite3(pointer: MemorySegment) : GenericPointer(pointer)

public actual class sqlite3_context(pointer: MemorySegment) : GenericPointer(pointer)

public actual class sqlite3_stmt(pointer: MemorySegment) : GenericPointer(pointer)

public actual class sqlite3_value(pointer: MemorySegment) : GenericPointer(pointer)
