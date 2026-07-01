package ksqlite.capi.vfs

import ksqlite.capi.memory.JniPointer
import ksqlite.capi.memory.PointerOutputParam
import ksqlite.capi.memory.Struct

public actual class sqlite3_vfs internal constructor(pointer: JniPointer) : Struct(pointer) {

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_vfs>() {

        override fun create(pointer: Long): sqlite3_vfs = sqlite3_vfs(pointer)
    }
}