@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.memory.Struct
import ksqlite.capi.types.vtab.Sqlite3VTabCursor
import ksqlite.foreign.structs.invoke

public actual open class sqlite3_vtab_cursor public actual constructor() :
    Struct(capi.sqlite3_vtab_cursor()),
    Sqlite3VTabCursor