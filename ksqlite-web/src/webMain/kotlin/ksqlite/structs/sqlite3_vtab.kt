@file:Suppress("ClassName")

package ksqlite.structs

import ksqlite.wasm.WasmPointer

/**
 * JS wrapper around `sqlite3_vtab` C-struct.
 */
public external class sqlite3_vtab : JaccwabytStruct {

    public var pModule: WasmPointer
    public val nRef: Int
    public var zErrMsg: WasmPointer
}