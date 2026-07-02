@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.AllocatableStruct
import ksqlite.types.vtab.SqliteVtabCursor

public actual open class sqlite3_vtab_cursor private constructor(
    override val pointer: CPointer<s3_vtab_cursor>,
    owned: Boolean,
) : AllocatableStruct(pointer, owned),
    SqliteVtabCursor {

    public actual constructor() : this(allocate(), true)
}