@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.AllocatableStruct
import ksqlite.capi.memory.orNull
import ksqlite.types.vfs.SqliteFile
import ksqlite.foreign.structs.sqlite3_file as s3_file

public actual class sqlite3_file private constructor(private val file: s3_file) :
    AllocatableStruct(file),
    SqliteFile {

    public actual constructor(vfs: sqlite3_vfs) : this(s3_file(vfs.szOsFile))

    public actual val pMethods: sqlite3_io_methods? by lazy {
        file.pMethods.orNull?.let(::sqlite3_io_methods)
    }
}