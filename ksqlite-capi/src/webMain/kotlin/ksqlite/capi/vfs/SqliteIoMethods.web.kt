@file:Suppress("ClassName")

package ksqlite.capi.vfs

import ksqlite.capi.capi
import ksqlite.capi.memory.Struct
import ksqlite.capi.memory.usingJsFunction
import ksqlite.capi.vfs.callbacks.SqliteIoMethodsCloseCallback
import ksqlite.foreign.structs.invoke
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

    internal constructor(pointer: WasmPointer) : this(capi.sqlite3_io_methods(pointer))

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