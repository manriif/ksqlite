@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.AllocatedStruct
import ksqlite.types.vtab.SqliteVtabCursor

public actual open class sqlite3_vtab_cursor public actual constructor() :
    AllocatedStruct(s3_vtab_cursor.layout()),
    SqliteVtabCursor