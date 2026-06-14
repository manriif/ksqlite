@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct
import ksqlite.types.vtab.SqliteVTabCursor

public actual open class sqlite3_vtab_cursor private constructor(cursor: s3_vtab_cursor) :
    Struct(cursor),
    SqliteVTabCursor {

    public actual constructor() : this(s3_vtab_cursor())
}