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
package ksqlite.foreign.wasm

import kotlin.js.JsBigInt
import kotlin.js.JsName

/**
 * Wasm pointer type.
 */
public typealias WasmPointer = JsBigInt

/**
 * The [wasm.ptr](https://sqlite.org/wasm/doc/trunk/api-wasm.md#wasm-ptr) API was added to assist in
 * smoothing over the differences between 32- and 64-bit JS/WASM environments.
 */
public external interface WasmPtr {

    /**
     * A "null" pointer of type Number or BigInt. Equivalent to one of Number(0) or BigInt(0).
     *
     * This value is guaranteed to compare === to WASM NULL pointers and to compare ==0 (not ===0,
     * which only applies in 32-builds!).
     */
    @JsName("null")
    public val `null`: WasmPointer
}