@file:Suppress("ClassName")

package ksqlite.foreign.structs

import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.JsName

/**
 * JS wrapper around `sqlite3_io_methods` C-struct.
 */
public external interface sqlite3_io_methods : StructType {

    @JsName($$"$iVersion")
    public var iVersion: Int

    @JsName($$"$xClose")
    public var xClose: WasmPointer

    @JsName($$"$xRead")
    public var xRead: WasmPointer

    @JsName($$"$xWrite")
    public var xWrite: WasmPointer

    @JsName($$"$xTruncate")
    public var xTruncate: WasmPointer

    @JsName($$"$xSync")
    public var xSync: WasmPointer

    @JsName($$"$xFileSize")
    public var xFileSize: WasmPointer

    @JsName($$"$xLock")
    public var xLock: WasmPointer

    @JsName($$"$xUnlock")
    public var xUnlock: WasmPointer

    @JsName($$"$xCheckReservedLock")
    public var xCheckReservedLock: WasmPointer

    @JsName($$"$xFileControl")
    public var xFileControl: WasmPointer

    @JsName($$"$xSectorSize")
    public var xSectorSize: WasmPointer

    @JsName($$"$xDeviceCharacteristics")
    public var xDeviceCharacteristics: WasmPointer

    @JsName($$"$xShmMap")
    public var xShmMap: WasmPointer

    @JsName($$"$xShmLock")
    public var xShmLock: WasmPointer

    @JsName($$"$xShmBarrier")
    public var xShmBarrier: WasmPointer

    @JsName($$"$xShmUnmap")
    public var xShmUnmap: WasmPointer

    @JsName($$"$xFetch")
    public var xFetch: WasmPointer

    @JsName($$"$xUnfetch")
    public var xUnfetch: WasmPointer
}