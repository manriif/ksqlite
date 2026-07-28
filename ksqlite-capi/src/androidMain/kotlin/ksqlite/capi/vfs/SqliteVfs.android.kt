/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.memory.PointerOutputParam
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.useParam
import ksqlite.capi.memory.wrapOrNull
import ksqlite.capi.vfs.callbacks.SqliteVfsAccessCallback
import ksqlite.capi.vfs.callbacks.SqliteVfsDeleteCallback
import ksqlite.capi.vfs.callbacks.SqliteVfsOpenCallback
import ksqlite.foreign.JniPointer
import ksqlite.foreign.vfsAccess
import ksqlite.foreign.vfsDelete
import ksqlite.foreign.vfsOpen
import ksqlite.types.internal.convertResultCode
import ksqlite.types.internal.convertVfsVersion
import ksqlite.types.vfs.SqliteVfs
import ksqlite.types.vfs.SqliteVfsVersion
import ksqlite.foreign.structs.sqlite3_vfs as s3_vfs

public actual class sqlite3_vfs private constructor(private val vfs: s3_vfs) :
    Struct(vfs.pointer),
    SqliteVfs {

    internal constructor(pointer: JniPointer) : this(s3_vfs(pointer))

    public actual override val iVersion: SqliteVfsVersion
        get() = convertVfsVersion(vfs.iVersion)

    public actual override val szOsFile: Int
        get() = vfs.szOsFile

    public actual override val mxPathname: Int
        get() = vfs.mxPathname

    public actual override val pNext: sqlite3_vfs?
        get() = vfs.pNext.wrapOrNull(::sqlite3_vfs)

    public actual override val zName: String
        get() = vfs.zName.toKStringFromUtf8()

    public actual val xOpen: SqliteVfsOpenCallback by lazy {
        SqliteVfsOpenCallback { pVfs, fileName, file, flags, outFlags ->
            convertResultCode(useParam(outFlags?.base) { flagsPtr ->
                vfsOpen(vfs.xOpen, pVfs.pointer, fileName, file.pointer, flags.value, flagsPtr)
            })
        }
    }

    public actual val xDelete: SqliteVfsDeleteCallback by lazy {
        SqliteVfsDeleteCallback { pVfs, name, syncDir ->
            convertResultCode(vfsDelete(vfs.xDelete, pVfs.pointer, name, syncDir))
        }
    }

    public actual val xAccess: SqliteVfsAccessCallback by lazy {
        SqliteVfsAccessCallback { pVfs, name, flags, outFlags ->
            convertResultCode(useParam(outFlags) { flagsPtr ->
                vfsAccess(vfs.xAccess, pVfs.pointer, name, flags.value, flagsPtr)
            })
        }
    }

    public actual class OutputParam actual constructor() : PointerOutputParam<sqlite3_vfs>() {

        override fun create(pointer: JniPointer): sqlite3_vfs = sqlite3_vfs(pointer)
    }
}
