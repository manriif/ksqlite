@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.memory.AllocatedStruct
import ksqlite.capi.memory.Struct
import ksqlite.foreign.structs.invoke
import ksqlite.types.vtab.SqliteVtabCursor

public actual open class sqlite3_vtab_cursor public actual constructor() :
    AllocatedStruct(capi.sqlite3_vtab_cursor()),
    SqliteVtabCursor