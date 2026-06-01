@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.StructPointer

public actual open class sqlite3_vtab_cursor public actual constructor() :
    StructPointer(allocate = { s3_vtab_cursor.allocate(this) })