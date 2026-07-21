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
 * JS wrapper around `sqlite3_module` C-struct.
 */
public external interface sqlite3_module : StructType {

    @JsName($$"$iVersion")
	public var iVersion: Int

    @JsName($$"$xCreate")
	public var xCreate: WasmPointer

    @JsName($$"$xConnect")
	public var xConnect: WasmPointer

    @JsName($$"$xBestIndex")
	public var xBestIndex: WasmPointer

    @JsName($$"$xDisconnect")
	public var xDisconnect: WasmPointer

    @JsName($$"$xDestroy")
	public var xDestroy: WasmPointer

    @JsName($$"$xOpen")
	public var xOpen: WasmPointer

    @JsName($$"$xClose")
	public var xClose: WasmPointer

    @JsName($$"$xFilter")
	public var xFilter: WasmPointer

    @JsName($$"$xNext")
	public var xNext: WasmPointer

    @JsName($$"$xEof")
	public var xEof: WasmPointer

    @JsName($$"$xColumn")
	public var xColumn: WasmPointer

    @JsName($$"$xRowid")
	public var xRowid: WasmPointer

    @JsName($$"$xUpdate")
	public var xUpdate: WasmPointer

    @JsName($$"$xBegin")
	public var xBegin: WasmPointer

    @JsName($$"$xSync")
	public var xSync: WasmPointer

    @JsName($$"$xCommit")
	public var xCommit: WasmPointer

    @JsName($$"$xRollback")
	public var xRollback: WasmPointer

    @JsName($$"$xFindFunction")
	public var xFindFunction: WasmPointer

    @JsName($$"$xRename")
	public var xRename: WasmPointer

    @JsName($$"$xSavepoint")
	public var xSavepoint: WasmPointer

    @JsName($$"$xRelease")
	public var xRelease: WasmPointer

    @JsName($$"$xRollbackTo")
	public var xRollbackTo: WasmPointer

    @JsName($$"$xShadowName")
	public var xShadowName: WasmPointer

    @JsName($$"$xIntegrity")
	public var xIntegrity: WasmPointer
}