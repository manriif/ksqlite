@file:Suppress("ClassName")

package ksqlite.structs

import ksqlite.wasm.WasmPointer

/**
 * JS wrapper around `sqlite3_module` C-struct.
 */
public external class sqlite3_module : JaccwabytStruct {

    public var iVersion: Int
    public var xCreate: WasmPointer
    public var xConnect: WasmPointer
    public var xBestIndex: WasmPointer
    public var xDisconnect: WasmPointer
    public var xDestroy: WasmPointer
    public var xOpen: WasmPointer
    public var xClose: WasmPointer
    public var xFilter: WasmPointer
    public var xNext: WasmPointer
    public var xEof: WasmPointer
    public var xColumn: WasmPointer
    public var xRowid: WasmPointer
    public var xUpdate: WasmPointer
    public var xBegin: WasmPointer
    public var xSync: WasmPointer
    public var xCommit: WasmPointer
    public var xRollback: WasmPointer
    public var xFindFunction: WasmPointer
    public var xRename: WasmPointer
    public var xSavepoint: WasmPointer
    public var xRelease: WasmPointer
    public var xRollbackTo: WasmPointer
    public var xIntegrity: WasmPointer
}