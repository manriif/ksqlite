@file:Suppress("ClassName")

package ksqlite.foreign.structs

import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.JsName

/**
 * JS wrapper around `sqlite3_vfs` C-struct.
 */
public external interface sqlite3_vfs : StructType {

    @JsName($$"$iVersion")
    public var iVersion: Int

    @JsName($$"$szOsFile")
    public var szOsFile: Int

    @JsName($$"$mxPathname")
    public var mxPathname: Int

    @JsName($$"$pNext")
    public var pNext: WasmPointer

    @JsName($$"$zName")
    public var zName: WasmPointer

    @JsName($$"$pAppData")
    public var pAppData: WasmPointer

    @JsName($$"$xOpen")
    public var xOpen: WasmPointer

    @JsName($$"$xDelete")
    public var xDelete: WasmPointer

    @JsName($$"$xAccess")
    public var xAccess: WasmPointer

    @JsName($$"$xFullPathname")
    public var xFullPathname: WasmPointer

    @JsName($$"$xDlOpen")
    public var xDlOpen: WasmPointer

    @JsName($$"$xDlError")
    public var xDlError: WasmPointer

    @JsName($$"$xDlSym")
    public var xDlSym: WasmPointer

    @JsName($$"$xDlClose")
    public var xDlClose: WasmPointer

    @JsName($$"$xRandomness")
    public var xRandomness: WasmPointer

    @JsName($$"$xSleep")
    public var xSleep: WasmPointer

    @JsName($$"$xCurrentTime")
    public var xCurrentTime: WasmPointer

    @JsName($$"$xGetLastError")
    public var xGetLastError: WasmPointer

    @JsName($$"$xCurrentTimeInt64")
    public var xCurrentTimeInt64: WasmPointer

    @JsName($$"$xSetSystemCall")
    public var xSetSystemCall: WasmPointer

    @JsName($$"$xGetSystemCall")
    public var xGetSystemCall: WasmPointer

    @JsName($$"$xNextSystemCall")
    public var xNextSystemCall: WasmPointer
}