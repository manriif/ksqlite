@file:Suppress("ClassName")

package ksqlite.types

import ksqlite.memory.Pointer
import java.lang.foreign.MemorySegment

public actual class sqlite3_pointer(internal val pointer: MemorySegment)

public actual class sqlite3 : Pointer()

public actual class sqlite3_context : Pointer()

public actual class sqlite3_stmt : Pointer()
