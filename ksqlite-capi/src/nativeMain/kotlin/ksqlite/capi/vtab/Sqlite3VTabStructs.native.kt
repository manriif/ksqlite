@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.StructPointer
import ksqlite.sqlite3_index_info
import ksqlite.sqlite3_module
import ksqlite.sqlite3_vtab

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

internal typealias s3_index_info = sqlite3_index_info
internal typealias s3_module = sqlite3_module
internal typealias s3_vtab = sqlite3_vtab
internal typealias s3_vtab_cursor = sqlite3_vtab

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class sqlite3_index_info internal constructor(override val pointer: CPointer<s3_index_info>) :
    StructPointer(pointer),
    Sqlite3IndexInfo

public actual class sqlite3_module internal constructor(override val pointer: CPointer<s3_module>) :
    StructPointer(pointer)

public actual class sqlite3_vtab internal constructor(override val pointer: CPointer<s3_vtab>) :
    StructPointer(pointer)

public actual class sqlite3_vtab_cursor internal constructor(override val pointer: CPointer<s3_vtab_cursor>) :
    StructPointer(pointer)
