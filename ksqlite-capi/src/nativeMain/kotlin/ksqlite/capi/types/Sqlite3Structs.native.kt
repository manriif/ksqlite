@file:Suppress("ClassName")

package ksqlite.capi.types

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.StructPointer

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3 = cnames.structs.sqlite3
internal typealias s3_backup = cnames.structs.sqlite3_backup
internal typealias s3_blob = cnames.structs.sqlite3_blob
internal typealias s3_api = cnames.structs.sqlite3_api_routines
internal typealias s3_context = cnames.structs.sqlite3_context
internal typealias s3_stmt = cnames.structs.sqlite3_stmt
internal typealias s3_value = cnames.structs.sqlite3_value
internal typealias s3_snapshot = ksqlite.sqlite3_snapshot
internal typealias s3_vfs = ksqlite.sqlite3_vfs

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class sqlite3 internal constructor(override val pointer: CPointer<s3>) :
    StructPointer(pointer),
    MemoryScope

public actual class sqlite3_backup internal constructor(override val pointer: CPointer<s3_backup>) :
    StructPointer(pointer)

public actual class sqlite3_blob internal constructor(override val pointer: CPointer<s3_blob>) :
    StructPointer(pointer)

public actual class sqlite3_api_routines internal constructor(override val pointer: CPointer<s3_api>) :
    StructPointer(pointer)

public actual class sqlite3_context internal constructor(override val pointer: CPointer<s3_context>) :
    StructPointer(pointer)

public actual class sqlite3_snapshot internal constructor(override val pointer: CPointer<s3_snapshot>) :
    StructPointer(pointer)

public actual class sqlite3_stmt internal constructor(override val pointer: CPointer<s3_stmt>) :
    StructPointer(pointer),
    MemoryScope

public actual class sqlite3_value internal constructor(override val pointer: CPointer<s3_value>) :
    StructPointer(pointer)

public actual class sqlite3_vfs internal constructor(override val pointer: CPointer<s3_vfs>) :
    StructPointer(pointer)