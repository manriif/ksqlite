@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativeFreeablePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_mprintf
import ksqlite.types.vtab.SqliteVtab

public actual open class sqlite3_vtab private constructor(
    override val pointer: CPointer<s3_vtab>,
    placement: NativeFreeablePlacement? = null
) : Struct(pointer, placement),
    MemoryScope,
    SqliteVtab {

    public actual constructor() : this(nativeHeap.alloc<s3_vtab>().ptr, nativeHeap)

    public actual override val nRef: Int
        get() = pointer.pointed.nRef

    public actual override var errMsg: String?
        get() = pointer.pointed.zErrMsg?.toKStringFromUtf8()
        set(value) = pointer.pointed.run {
            sqlite3_free(zErrMsg)
            zErrMsg = value?.let { sqlite3_mprintf(it) }
        }
}