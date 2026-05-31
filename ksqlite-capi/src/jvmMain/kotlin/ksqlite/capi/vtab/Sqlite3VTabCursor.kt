package ksqlite.capi.vtab

import ksqlite.capi.memory.StructPointer
import java.lang.foreign.MemorySegment

public actual class sqlite3_vtab_cursor internal constructor(pointer: MemorySegment) :
    StructPointer(pointer)