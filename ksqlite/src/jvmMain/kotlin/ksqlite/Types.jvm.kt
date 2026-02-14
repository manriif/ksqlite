@file:Suppress("ClassName")

package ksqlite

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

public actual class pointer(internal val pointer: MemorySegment)

public actual class sqlite3(internal val pointer: MemorySegment) : PointerManager()

public actual class sqlite3_context(internal val pointer: MemorySegment)

public actual class sqlite3_stmt(internal val pointer: MemorySegment) : PointerManager()
