@file:Suppress("ClassName")
@file:OptIn(ExperimentalForeignApi::class)

package ksqlite.capi.types

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import ksqlite.capi.memory.GenericPointer

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3 = cnames.structs.sqlite3
internal typealias s3_backup = cnames.structs.sqlite3_backup
internal typealias s3_blob = cnames.structs.sqlite3_blob
internal typealias s3_api = cnames.structs.sqlite3_api_routines
internal typealias s3_context = cnames.structs.sqlite3_context
internal typealias s3_index_info = ksqlite.sqlite3_index_info
internal typealias s3_module = ksqlite.sqlite3_module
internal typealias s3_stmt = cnames.structs.sqlite3_stmt
internal typealias s3_value = cnames.structs.sqlite3_value
internal typealias s3_snapshot = ksqlite.sqlite3_snapshot
internal typealias s3_vfs = ksqlite.sqlite3_vfs

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class sqlite3 internal constructor(override val pointer: CPointer<s3>) :
    GenericPointer(pointer)

public actual class sqlite3_backup internal constructor(override val pointer: CPointer<s3_backup>) :
    GenericPointer(pointer)

public actual class sqlite3_blob internal constructor(override val pointer: CPointer<s3_blob>) :
    GenericPointer(pointer)

public actual class sqlite3_api_routines internal constructor(override val pointer: CPointer<s3_api>) :
    GenericPointer(pointer)

public actual class sqlite3_context internal constructor(override val pointer: CPointer<s3_context>) :
    GenericPointer(pointer)

public actual class sqlite3_index_info internal constructor(override val pointer: CPointer<s3_index_info>) :
    GenericPointer(pointer)

public actual class sqlite3_module internal constructor(override val pointer: CPointer<s3_module>) :
    GenericPointer(pointer)

public actual class sqlite3_snapshot internal constructor(override val pointer: CPointer<s3_snapshot>) :
    GenericPointer(pointer)

public actual class sqlite3_stmt internal constructor(override val pointer: CPointer<s3_stmt>) :
    GenericPointer(pointer)

public actual class sqlite3_value internal constructor(override val pointer: CPointer<s3_value>) :
    GenericPointer(pointer)

public actual class sqlite3_vfs internal constructor(override val pointer: CPointer<s3_vfs>) :
    GenericPointer(pointer)