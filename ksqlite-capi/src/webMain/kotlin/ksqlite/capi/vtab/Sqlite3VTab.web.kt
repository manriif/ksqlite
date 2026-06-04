@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.exports
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.structs.invoke

public actual abstract class sqlite3_vtab internal constructor(private val vTab: s3_vtab) :
    Struct(vTab),
    MemoryScope {

    public actual constructor() : this(capi.sqlite3_vtab())

    public actual val nRef: Int
        get() = vTab.nRef

    public actual var errMsg: String?
        get() = vTab.zErrMsg.toKStringFromUtf8OrNull()
        set(value) {
            exports.sqlite3_free(vTab.zErrMsg)
            vTab.zErrMsg = sqlite3_mprintf(value)
        }
}