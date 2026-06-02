@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.sqlite3

public actual abstract class sqlite3_vtab public actual constructor() :
    Struct(allocate = { s3_vtab.allocate(this) }),
    MemoryScope {

    public actual val nRef: Int
        get() = s3_vtab.nRef(pointer)

    public actual var errMsg: String?
        get() = s3_vtab.zErrMsg(pointer).toKStringFromUtf8OrNull()
        set(value) {
            sqlite3.sqlite3_free(s3_vtab.zErrMsg(pointer))
            s3_vtab.zErrMsg(pointer, sqlite3_mprintf(value))
        }
}