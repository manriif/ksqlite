@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct
import ksqlite.types.vtab.SqliteVtabCursor

public actual open class sqlite3_vtab_cursor public actual constructor() :
    Struct(allocate = s3_vtab_cursor::allocate),
    SqliteVtabCursor