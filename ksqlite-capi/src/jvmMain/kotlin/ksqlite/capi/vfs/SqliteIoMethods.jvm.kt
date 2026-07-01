@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.Struct
import ksqlite.capi.vfs.callbacks.SqliteIoMethodsCloseCallback
import ksqlite.types.internal.convertIoMethodsVersion
import ksqlite.types.internal.convertResultCode
import ksqlite.types.vfs.SqliteIoMethods
import ksqlite.types.vfs.SqliteIoMethodsVersion
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import ksqlite.foreign.sqlite3_io_methods as s3_io_methods

public actual class sqlite3_io_methods internal constructor(pointer: MemorySegment) :
    Struct(Arena.ofConfined(), { s3_io_methods.reinterpret(pointer, this, null) }),
    SqliteIoMethods {

    public actual override val iVersion: SqliteIoMethodsVersion
        get() = convertIoMethodsVersion(s3_io_methods.iVersion(pointer))

    public actual val xClose: SqliteIoMethodsCloseCallback by lazy {
        SqliteIoMethodsCloseCallback { file ->
            convertResultCode(
                s3_io_methods.xClose.invoke(
                    s3_io_methods.xClose(pointer),
                    file.pointer
                )
            )
        }
    }
}