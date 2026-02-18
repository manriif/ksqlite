@file:Suppress("ClassName")

package ksqlite.types

import ksqlite.memory.MemoryManager
import java.lang.foreign.MemorySegment

public actual class pointer(internal val pointer: MemorySegment)

public actual class sqlite3(internal val pointer: MemorySegment) : MemoryManager()

public actual class sqlite3_context(internal val pointer: MemorySegment)

public actual class sqlite3_stmt(internal val pointer: MemorySegment) : MemoryManager()
