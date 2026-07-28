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

import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.usingJsFunction
import ksqlite.capi.vfs.callbacks.SqliteIoMethodsCloseCallback
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.types.internal.convertIoMethodsVersion
import ksqlite.types.internal.convertResultCode
import ksqlite.types.vfs.SqliteIoMethods
import ksqlite.types.vfs.SqliteIoMethodsVersion
import ksqlite.foreign.structs.sqlite3_io_methods as s3_io_methods

public actual class sqlite3_io_methods private constructor(private val methods: s3_io_methods) :
    Struct(methods.pointer),
    SqliteIoMethods {

    internal constructor(pointer: WasmPointer) : this(s3_io_methods(pointer))

    public actual override val iVersion: SqliteIoMethodsVersion
        get() = convertIoMethodsVersion(methods.iVersion)

    public actual val xClose: SqliteIoMethodsCloseCallback by lazy {
        methods.xClose.usingJsFunction { function ->
            SqliteIoMethodsCloseCallback { file ->
                convertResultCode(xClose(function, file.pointer))
            }
        }
    }
}

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

@JsFun("(fn, p0) => fn(p0)")
private external fun xClose(
    fn: JsFunction,
    p0: WasmPointer
): Int