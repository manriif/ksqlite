@file:Suppress("ClassName")

package ksqlite.capi.vtab

import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3_mprintf

public actual abstract class sqlite3_vtab private constructor(private val vTab: s3_vtab) :
    Struct(vTab),
    MemoryScope {

    public actual constructor() : this(s3_vtab())

    public actual val nRef: Int
        get() = vTab.nRef

    public actual var errMsg: String?
        get() = vTab.zErrMsg.toKStringFromUtf8OrNull()
        set(value) = sqlite3_mprintf(vTab::zErrMsg, value)
}