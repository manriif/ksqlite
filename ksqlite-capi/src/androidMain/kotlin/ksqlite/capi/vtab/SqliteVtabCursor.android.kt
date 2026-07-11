@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.AllocatedStruct
import ksqlite.types.vtab.SqliteVtabCursor

public actual open class sqlite3_vtab_cursor private constructor(cursor: s3_vtab_cursor) :
    AllocatedStruct(cursor),
    SqliteVtabCursor {

    public actual constructor() : this(s3_vtab_cursor())
}