@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.orNull
import ksqlite.types.vfs.SqliteFile
import ksqlite.foreign.sqlite3_file as s3_file

public actual class sqlite3_file public actual constructor(vfs: sqlite3_vfs) :
    Struct(allocate = s3_file::allocate),
    SqliteFile {

    public actual val pMethods: sqlite3_io_methods? by lazy {
        s3_file.pMethods(pointer).orNull?.let(::sqlite3_io_methods)
    }

    init {
        s3_file.reinterpret()
    }
}