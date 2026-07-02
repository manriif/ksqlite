@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import ksqlite.capi.memory.AllocatableStruct
import ksqlite.capi.memory.MemoryScope
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_mprintf
import ksqlite.types.vtab.SqliteVtab

public actual open class sqlite3_vtab private constructor(
    override val pointer: CPointer<s3_vtab>,
    owned: Boolean,
) : AllocatableStruct(pointer, owned),
    MemoryScope,
    SqliteVtab {

    public actual constructor() : this(allocate(), true)

    public actual override val nRef: Int
        get() = pointer.pointed.nRef

    public actual override var errMsg: String?
        get() = pointer.pointed.zErrMsg?.toKStringFromUtf8()
        set(value) = pointer.pointed.run {
            sqlite3_free(zErrMsg)
            zErrMsg = value?.let { sqlite3_mprintf(it) }
        }
}