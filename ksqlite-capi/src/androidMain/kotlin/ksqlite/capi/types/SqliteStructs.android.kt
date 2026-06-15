@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.JniPointer
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct

public actual class sqlite3 internal constructor(pointer: JniPointer) :
    Struct(pointer),
    MemoryScope

public actual class sqlite3_backup internal constructor(pointer: JniPointer) :
    Struct(pointer)

public actual class sqlite3_blob internal constructor(pointer: JniPointer) :
    Struct(pointer)

public actual class sqlite3_context internal constructor(pointer: JniPointer) :
    Struct(pointer)

public actual class sqlite3_snapshot internal constructor(pointer: JniPointer) :
    Struct(pointer)

public actual class sqlite3_stmt internal constructor(pointer: JniPointer) :
    Struct(pointer),
    MemoryScope

public actual class sqlite3_value internal constructor(pointer: JniPointer) :
    Struct(pointer)

public actual class sqlite3_vfs internal constructor(pointer: JniPointer) :
    Struct(pointer)