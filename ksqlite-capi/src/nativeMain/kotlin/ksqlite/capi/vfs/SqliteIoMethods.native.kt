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

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.invoke
import kotlinx.cinterop.pointed
import ksqlite.capi.memory.Struct
import ksqlite.capi.vfs.callbacks.SqliteIoMethodsCloseCallback
import ksqlite.types.internal.convertIoMethodsVersion
import ksqlite.types.internal.convertResultCode
import ksqlite.types.vfs.SqliteIoMethods
import ksqlite.types.vfs.SqliteIoMethodsVersion
import ksqlite.foreign.sqlite3_io_methods as s3_io_methods

public actual class sqlite3_io_methods
internal constructor(override val pointer: CPointer<s3_io_methods>) :
    Struct(pointer),
    SqliteIoMethods {

    public actual override val iVersion: SqliteIoMethodsVersion
        get() = convertIoMethodsVersion(pointer.pointed.iVersion)

    public actual val xClose: SqliteIoMethodsCloseCallback by lazy {
        SqliteIoMethodsCloseCallback { file ->
            convertResultCode(pointer.pointed.xClose!!.invoke(file.pointer))
        }
    }
}