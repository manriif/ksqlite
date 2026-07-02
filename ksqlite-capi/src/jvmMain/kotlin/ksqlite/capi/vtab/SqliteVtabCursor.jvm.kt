@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.AllocatableStruct
import ksqlite.capi.memory.OwnedStruct
import ksqlite.capi.memory.Struct
import ksqlite.types.vtab.SqliteVtabCursor

public actual open class sqlite3_vtab_cursor public actual constructor() :
    AllocatableStruct(s3_vtab_cursor.layout(), s3_vtab_cursor::reinterpret),
    SqliteVtabCursor