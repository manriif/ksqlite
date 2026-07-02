@file:Suppress("ClassName")

package ksqlite.capi.vfs

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.pointed
import ksqlite.capi.memory.AllocatedStruct
import ksqlite.types.vfs.SqliteFile
import ksqlite.foreign.sqlite3_file as s3_file

public actual class sqlite3_file
private constructor(override val pointer: CPointer<s3_file>) :
    AllocatedStruct(pointer),
    SqliteFile {

    public actual constructor(vfs: sqlite3_vfs) : this(allocate(vfs.szOsFile.toLong()))

    public actual val pMethods: sqlite3_io_methods? by lazy {
        pointer.pointed.pMethods?.let(::sqlite3_io_methods)
    }
}