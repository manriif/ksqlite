@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.StructPointer

public actual class sqlite3 internal constructor(pointer: Long) :
    StructPointer(pointer),
    MemoryScope

public actual class sqlite3_backup internal constructor(pointer: Long) :
    StructPointer(pointer)

public actual class sqlite3_blob internal constructor(pointer: Long) :
    StructPointer(pointer)

public actual class sqlite3_api_routines internal constructor(pointer: Long) :
    StructPointer(pointer)

public actual class sqlite3_context internal constructor(pointer: Long) :
    StructPointer(pointer)

public actual class sqlite3_snapshot internal constructor(pointer: Long) :
    StructPointer(pointer)

public actual class sqlite3_stmt internal constructor(pointer: Long) :
    StructPointer(pointer),
    MemoryScope

public actual class sqlite3_value internal constructor(pointer: Long) :
    StructPointer(pointer)

public actual class sqlite3_vfs internal constructor(pointer: Long) :
    StructPointer(pointer)