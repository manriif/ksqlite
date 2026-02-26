@file:Suppress("ClassName")
@file:OptIn(ExperimentalForeignApi::class)

package ksqlite.capi.types

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import ksqlite.capi.memory.ManagedPointer
import ksqlite.capi.memory.SimplePointer

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3 = cnames.structs.sqlite3
internal typealias s3_backup = cnames.structs.sqlite3_backup
internal typealias s3_blob = cnames.structs.sqlite3_blob
internal typealias s3_api_routines = cnames.structs.sqlite3_api_routines
internal typealias s3_context = cnames.structs.sqlite3_context
internal typealias s3_stmt = cnames.structs.sqlite3_stmt
internal typealias s3_value = cnames.structs.sqlite3_value
internal typealias s3_snapshot = ksqlite.sqlite3_snapshot

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class sqlite3 internal constructor(
    pointer: CPointer<s3>,
    restricted: Boolean = true
) : ManagedPointer<s3>(pointer, restricted)

public actual class sqlite3_backup internal constructor(
    pointer: CPointer<s3_backup>
) : SimplePointer<s3_backup>(pointer)

public actual class sqlite3_blob internal constructor(
    pointer: CPointer<s3_blob>,
) : SimplePointer<s3_blob>(pointer)

public actual class sqlite3_api_routines internal constructor(
    pointer: CPointer<s3_api_routines>
) : SimplePointer<s3_api_routines>(pointer)

public actual class sqlite3_context internal constructor(
    pointer: CPointer<s3_context>
) : SimplePointer<s3_context>(pointer)

public actual class sqlite3_stmt internal constructor(
    pointer: CPointer<s3_stmt>,
    restricted: Boolean = true
) : ManagedPointer<s3_stmt>(pointer, restricted)

public actual class sqlite3_value internal constructor(
    pointer: CPointer<s3_value>
) : SimplePointer<s3_value>(pointer)

public actual class sqlite3_snapshot internal constructor(
    pointer: CPointer<s3_snapshot>,
) : SimplePointer<s3_snapshot>(pointer)
