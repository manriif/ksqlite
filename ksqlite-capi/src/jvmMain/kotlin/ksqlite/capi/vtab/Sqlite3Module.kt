package ksqlite.capi.vtab

import ksqlite.capi.memory.StructPointer
import java.lang.foreign.MemorySegment

public actual class sqlite3_module internal constructor(pointer: MemorySegment) :
    StructPointer(pointer)