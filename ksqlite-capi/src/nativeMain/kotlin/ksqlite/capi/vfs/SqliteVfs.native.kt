@file:Suppress("ClassName")

package ksqlite.capi.vfs

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.cstr
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.toKStringFromUtf8
import ksqlite.capi.memory.PointerOutputParam
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.useParam
import ksqlite.capi.vfs.callbacks.SqliteVfsAccessCallback
import ksqlite.capi.vfs.callbacks.SqliteVfsDeleteCallback
import ksqlite.capi.vfs.callbacks.SqliteVfsOpenCallback
import ksqlite.types.internal.convertResultCode
import ksqlite.types.internal.convertVfsVersion
import ksqlite.types.vfs.SqliteVfs
import ksqlite.types.vfs.SqliteVfsVersion
import ksqlite.foreign.sqlite3_vfs as s3_vfs

public actual class sqlite3_vfs internal constructor(override val pointer: CPointer<s3_vfs>) :
    Struct(pointer),
    SqliteVfs {

    public actual override val iVersion: SqliteVfsVersion
        get() = convertVfsVersion(pointer.pointed.iVersion)

    public actual override val szOsFile: Int
        get() = pointer.pointed.szOsFile

    public actual override val mxPathname: Int
        get() = pointer.pointed.mxPathname

    public actual override val zName: String
        get() = pointer.pointed.zName!!.toKStringFromUtf8()

    public actual val xOpen: SqliteVfsOpenCallback by lazy {
        SqliteVfsOpenCallback { vfs, name, file, flags, outFlags ->
            convertResultCode(memScoped {
                useParam(outFlags?.base) { flagsPtr ->
                    pointer.pointed.xOpen!!.invoke(
                        vfs.pointer,
                        name?.cstr?.ptr,
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
                pointer.pointed.xDelete!!.invoke(
                    vfs.pointer,
                    name.cstr.ptr,
                    syncDir
                )
            })
        }
    }

    public actual val xAccess: SqliteVfsAccessCallback by lazy {
        SqliteVfsAccessCallback { vfs, name, flags, outFlags ->
            convertResultCode(memScoped {
                useParam(outFlags?.base) { flagsPtr ->
                    pointer.pointed.xAccess!!.invoke(
                        vfs.pointer,
                        name.cstr.ptr,
                        flags.value,
                        flagsPtr
                    )
                }
            })
        }
    }

    public actual class OutputParam actual constructor() :
        PointerOutputParam<sqlite3_vfs, s3_vfs>() {

        override fun create(pointer: CPointer<s3_vfs>): sqlite3_vfs = sqlite3_vfs(pointer)
    }
}