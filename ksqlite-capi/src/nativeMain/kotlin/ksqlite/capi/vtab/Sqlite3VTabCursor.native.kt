@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.StructPointer

public actual open class sqlite3_vtab_cursor internal constructor(override val pointer: CPointer<s3_vtab_cursor>) :
    StructPointer(pointer),
    MemoryScope {

    public actual constructor() : this(nativeHeap.alloc<s3_vtab_cursor>().ptr)
}