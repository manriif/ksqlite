@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.capi
import ksqlite.capi.memory.DeallocStruct
import ksqlite.capi.memory.orNull
import ksqlite.capi.wasm
import ksqlite.foreign.structs.invoke
import ksqlite.types.vfs.SqliteFile
import ksqlite.foreign.structs.sqlite3_file as s3_file

public actual class sqlite3_file private constructor(private val file: s3_file) :
    DeallocStruct(file),
    SqliteFile {

    public actual constructor(vfs: sqlite3_vfs) : this(capi.sqlite3_file(wasm.alloc(vfs.szOsFile)))

    public actual val pMethods: sqlite3_io_methods? by lazy {
        file.pMethods.orNull?.let(::sqlite3_io_methods)
    }
}