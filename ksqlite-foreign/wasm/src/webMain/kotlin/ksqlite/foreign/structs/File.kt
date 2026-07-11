@file:Suppress("ClassName")

package ksqlite.foreign.structs

import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.JsName

/**
 * JS wrapper around `sqlite3_file` C-struct.
 */
public external interface sqlite3_file : StructType {

    @JsName($$"$pMethods")
    public var pMethods: WasmPointer
}