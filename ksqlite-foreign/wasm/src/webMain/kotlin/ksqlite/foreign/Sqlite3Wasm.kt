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
package ksqlite.foreign

import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmMemory
import kotlin.js.JsAny

/**
 * The [sqlite3.wasm](https://sqlite.org/wasm/doc/trunk/api-wasm.md) namespace, abbreviated as wasm
 * for the remainder of this page, holds a number of routines for working with WASM-side constructs.
 * They include APIs for such tasks as...
 *
 * - Memory management.
 *     - Allocating and freeing memory.
 *     - Helpers for working with WASM heap memory, e.g. getting and setting primitive values
 *     from/to the WASM heap.
 * - Configurable result value and argument type conversion for WASM-exported functions.
 * - JS/C String conversions.
 * - Binding JS functions into the WASM runtime, so that they may be called from WASM code (i.e.
 * from C).
 *
 * In short, if a WASM-specific feature has been needed during the development of the sqlite3 JS API,
 * it's been added to this namespace. For the most part, high-level client code will rarely need to
 * make use of more than a few of these, whereas clients using the C-style APIs may make heavy use
 * of them.
 */
public external interface Sqlite3Wasm : JsAny, WasmFunctions, WasmMemory {

    /**
     * Wasm exports namespace.
     */
    public val exports: Sqlite3WasmExports
}