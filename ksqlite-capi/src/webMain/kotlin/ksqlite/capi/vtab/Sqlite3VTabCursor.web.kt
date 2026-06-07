@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.memory.Struct
import ksqlite.structs.invoke

public actual open class sqlite3_vtab_cursor public actual constructor() :
    Struct(capi.sqlite3_vtab_cursor())