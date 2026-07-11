@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import ksqlite.capi.memory.AllocatedStruct
import ksqlite.types.vtab.SqliteVtabCursor

public actual open class sqlite3_vtab_cursor
private constructor(override val pointer: CPointer<s3_vtab_cursor>) :
    AllocatedStruct(pointer),
    SqliteVtabCursor {

    public actual constructor() : this(allocate())
}