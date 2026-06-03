@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct

public actual open class sqlite3_vtab_cursor public actual constructor() :
    Struct(s3_vtab_cursor())