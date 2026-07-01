@file:Suppress("ClassName")

package ksqlite.capi.vtab

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativeFreeablePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import ksqlite.capi.memory.Struct
import ksqlite.types.vtab.SqliteVtabCursor

public actual open class sqlite3_vtab_cursor private constructor(
    override val pointer: CPointer<s3_vtab_cursor>,
    placement: NativeFreeablePlacement? = null
) : Struct(pointer, placement),
    SqliteVtabCursor {

    public actual constructor() : this(nativeHeap.alloc<s3_vtab_cursor>().ptr, nativeHeap)
}