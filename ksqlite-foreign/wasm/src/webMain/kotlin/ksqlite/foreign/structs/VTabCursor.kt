@file:Suppress("ClassName")

package ksqlite.foreign.structs

import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.JsName

/**
 * JS wrapper around `sqlite3_vtab_cursor` C-struct.
 */
public external interface sqlite3_vtab_cursor : StructType {

    @JsName($$"$pVtab")
    public var pVtab: WasmPointer
}