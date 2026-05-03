@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.GenericPointer
import java.lang.foreign.MemorySegment

public actual class sqlite3 internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_backup internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_blob internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_api_routines internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_context internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_index_info internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_module internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_snapshot internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_stmt internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_value internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)

public actual class sqlite3_vfs internal constructor(pointer: MemorySegment) :
    GenericPointer(pointer)
