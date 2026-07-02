@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.PointerOutputParam
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.useParam
import ksqlite.capi.vfs.callbacks.SqliteVfsOpenCallback
import ksqlite.foreign.vfsOpen
import ksqlite.types.internal.convertResultCode
import ksqlite.types.internal.convertVfsVersion
import ksqlite.types.vfs.SqliteVfs
import ksqlite.types.vfs.SqliteVfsVersion
import ksqlite.foreign.structs.sqlite3_vfs as s3_vfs

public actual class sqlite3_vfs private constructor(private val vfs: s3_vfs) :
    Struct(vfs.pointer),
    SqliteVfs {

    internal constructor(pointer: Long) : this(s3_vfs(pointer))

    public actual override val iVersion: SqliteVfsVersion
        get() = convertVfsVersion(vfs.iVersion)

    public actual override val szOsFile: Int
        get() = vfs.szOsFile

    public actual override val mxPathname: Int
        get() = vfs.mxPathname

    public actual override val zName: String
        get() = vfs.zName.toKStringFromUtf8()

    public actual val xOpen: SqliteVfsOpenCallback by lazy {
        SqliteVfsOpenCallback { vfsIn, fileName, file, flags, outFlags ->
            convertResultCode(useParam(outFlags?.base) { flagsPtr ->
                vfsOpen(vfs.xOpen, vfsIn.pointer, fileName, file.pointer, flags.value, flagsPtr)
            })
        }
    }

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_vfs>() {

        override fun create(pointer: Long): sqlite3_vfs = sqlite3_vfs(pointer)
    }
}
