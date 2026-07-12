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