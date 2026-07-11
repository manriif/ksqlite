@file:Suppress("ClassName")

package ksqlite.foreign.structs

import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.JsName

/**
 * JS wrapper around `sqlite3_vtab` C-struct.
 */
public external interface sqlite3_vtab : StructType {

    @JsName($$"$pModule")
    public var pModule: WasmPointer

    @JsName($$"$nRef")
    public var nRef: Int

    @JsName($$"$zErrMsg")
    public var zErrMsg: WasmPointer
}