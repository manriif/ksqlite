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
@file:Suppress("DEPRECATION")

package ksqlite.foreign.wasm

import kotlin.js.JsAny
import kotlin.js.nativeInvoke

/**
 * Wasm alloc function.
 */
public external interface WasmAlloc : JsAny {

    /**
     * Invokes the base function, which can throws.
     */
    @nativeInvoke
    public operator fun invoke(n: Int): WasmPointer

    /**
     * Invokes the impl function, which does not throw.
     */
    public fun impl(n: Int): WasmPointer
}

/**
 * Wasm realloc function.
 */
public external interface WasmRealloc : JsAny {

    /**
     * Invokes the base function, which can throws.
     */
    @nativeInvoke
    public operator fun invoke(ptr: WasmPointer, size: Int): WasmPointer

    /**
     * Invokes the impl function, which does not throw.
     */
    public fun impl(ptr: WasmPointer, size: Int): WasmPointer
}