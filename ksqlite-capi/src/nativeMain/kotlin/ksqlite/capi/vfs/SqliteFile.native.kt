@file:Suppress("ClassName")

package ksqlite.capi.vfs

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.NativeFreeablePlacement
import kotlinx.cinterop.alloc
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import ksqlite.capi.memory.Struct
import ksqlite.types.vfs.SqliteFile
import ksqlite.foreign.sqlite3_file as s3_file

public actual class sqlite3_file internal constructor(
    override val pointer: CPointer<s3_file>,
    placement: NativeFreeablePlacement? = null
) : Struct(pointer, placement),
    SqliteFile {

    public actual constructor() : this(nativeHeap.alloc<s3_file>().ptr, nativeHeap)

    public actual val pMethods: sqlite3_io_methods? by lazy {
        pointer.pointed.pMethods?.let(::sqlite3_io_methods)
    }
}