@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.PointerOutputParam
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.memScoped
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.useParam
import ksqlite.capi.vfs.callbacks.SqliteVfsAccessCallback
import ksqlite.capi.vfs.callbacks.SqliteVfsDeleteCallback
import ksqlite.capi.vfs.callbacks.SqliteVfsOpenCallback
import ksqlite.types.internal.convertResultCode
import ksqlite.types.internal.convertVfsVersion
import ksqlite.types.vfs.SqliteVfs
import ksqlite.types.vfs.SqliteVfsVersion
import java.lang.foreign.MemorySegment
import ksqlite.foreign.sqlite3_vfs as s3_vfs

public actual class sqlite3_vfs internal constructor(pointer: MemorySegment) :
    Struct(pointer),
    SqliteVfs {

    public actual override val iVersion: SqliteVfsVersion
        get() = convertVfsVersion(s3_vfs.iVersion(pointer))

    public actual override val szOsFile: Int
        get() = s3_vfs.szOsFile(pointer)

    public actual override val mxPathname: Int
        get() = s3_vfs.mxPathname(pointer)

    public actual override val zName: String
        get() = s3_vfs.zName(pointer).toKStringFromUtf8()

    public actual val xOpen: SqliteVfsOpenCallback by lazy {
        SqliteVfsOpenCallback { vfs, name, file, flags, outFlags ->
            convertResultCode(memScoped {
                useParam(outFlags?.base) { flagsPtr ->
                    s3_vfs.xOpen.invoke(
                        s3_vfs.xOpen(pointer),
                        vfs.pointer,
                        name.allocateUtf8(),
                        file.pointer,
                        flags.value,
                        flagsPtr
                    )
                }
            })
        }
    }

    public actual val xDelete: SqliteVfsDeleteCallback by lazy {
        SqliteVfsDeleteCallback { vfs, name, syncDir ->
            convertResultCode(memScoped {
                s3_vfs.xDelete.invoke(
                    s3_vfs.xDelete(pointer),
                    vfs.pointer,
                    name.allocateUtf8(),
                    syncDir
                )
            })
        }
    }

    public actual val xAccess: SqliteVfsAccessCallback by lazy {
        SqliteVfsAccessCallback { vfs, name, flags, outFlags ->
            convertResultCode(memScoped {
                useParam(outFlags?.base) { flagsPtr ->
                    s3_vfs.xAccess.invoke(
                        s3_vfs.xAccess(pointer),
                        vfs.pointer,
                        name.allocateUtf8(),
                        flags.value,
                        flagsPtr
                    )
                }
            })
        }
    }

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_vfs>() {

        override fun create(pointer: MemorySegment): sqlite3_vfs = sqlite3_vfs(pointer)
    }
}