@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.GenericPointer

public actual class sqlite3 internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_backup internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_blob internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_api_routines internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_context internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_index_info internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_module internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_snapshot internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_stmt internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_value internal constructor(pointer: Long) :
    GenericPointer(pointer)

public actual class sqlite3_vfs internal constructor(pointer: Long) :
    GenericPointer(pointer)
