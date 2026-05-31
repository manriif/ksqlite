@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.StructPointer
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.sqlite3_free
import ksqlite.sqlite3_mprintf

public actual abstract class sqlite3_vtab internal constructor(override val pointer: CPointer<s3_vtab>) :
    StructPointer(pointer),
    MemoryScope {

    public actual constructor() : this(nativeHeap.alloc<s3_vtab>().ptr)

    public actual val nRef: Int
        get() = pointer.pointed.nRef

    public actual var errMsg: String?
        get() = pointer.pointed.zErrMsg?.toKStringFromUtf8()
        set(value) = pointer.pointed.run {
            sqlite3_free(zErrMsg)
            zErrMsg = value?.let(::sqlite3_mprintf)
        }
}