@file:Suppress("ClassName")

package ksqlite.capi

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.PointerOutputParam
import ksqlite.capi.memory.Struct

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
internal typealias s3_snapshot = ksqlite.foreign.sqlite3_snapshot

///////////////////////////////////////////////////////////////////////////
// Structs
///////////////////////////////////////////////////////////////////////////

public actual class sqlite3 internal constructor(override val pointer: CPointer<s3>) :
    Struct(pointer),
    MemoryScope {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3, s3>() {

        override fun create(pointer: CPointer<s3>): sqlite3 = sqlite3(pointer)
    }
}

public actual class sqlite3_backup internal constructor(override val pointer: CPointer<s3_backup>) :
    Struct(pointer)

public actual class sqlite3_blob internal constructor(override val pointer: CPointer<s3_blob>) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() :
        PointerOutputParam<sqlite3_blob, s3_blob>() {

        override fun create(pointer: CPointer<s3_blob>): sqlite3_blob = sqlite3_blob(pointer)
    }
}

public actual class sqlite3_context internal constructor(override val pointer: CPointer<s3_context>) :
    Struct(pointer)

public actual class sqlite3_snapshot internal constructor(override val pointer: CPointer<s3_snapshot>) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() :
        PointerOutputParam<sqlite3_snapshot, s3_snapshot>() {

        override fun create(pointer: CPointer<s3_snapshot>): sqlite3_snapshot =
            sqlite3_snapshot(pointer)
    }
}

public actual class sqlite3_stmt internal constructor(override val pointer: CPointer<s3_stmt>) :
    Struct(pointer),
    MemoryScope {

    public actual class OutputParam actual constructor() :
        PointerOutputParam<sqlite3_stmt, s3_stmt>() {

        override fun create(pointer: CPointer<s3_stmt>): sqlite3_stmt = sqlite3_stmt(pointer)
    }
}

public actual class sqlite3_value internal constructor(override val pointer: CPointer<s3_value>) :
    Struct(pointer) {

    public actual class OutputParam actual constructor() :
        PointerOutputParam<sqlite3_value, s3_value>() {

        override fun create(pointer: CPointer<s3_value>): sqlite3_value = sqlite3_value(pointer)
    }
}