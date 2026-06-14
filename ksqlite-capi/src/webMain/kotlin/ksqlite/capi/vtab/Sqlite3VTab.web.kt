@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.capi
import ksqlite.capi.exports
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf
import ksqlite.capi.types.vtab.Sqlite3VTab
import ksqlite.foreign.structs.invoke

public actual open class sqlite3_vtab private constructor(private val vTab: s3_vtab) :
    Struct(vTab),
    MemoryScope,
    Sqlite3VTab {

    public actual constructor() : this(capi.sqlite3_vtab())

    public actual override val nRef: Int
        get() = vTab.nRef

    public actual override var errMsg: String?
        get() = vTab.zErrMsg.toKStringFromUtf8OrNull()
        set(value) {
            exports.sqlite3_free(vTab.zErrMsg)
            vTab.zErrMsg = sqlite3_mprintf(value)
        }
}