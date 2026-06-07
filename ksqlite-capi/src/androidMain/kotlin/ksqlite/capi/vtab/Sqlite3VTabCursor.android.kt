@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.Struct

public actual open class sqlite3_vtab_cursor private constructor(cursor: s3_vtab_cursor) :
    Struct(cursor) {

    public actual constructor() : this(s3_vtab_cursor())
}