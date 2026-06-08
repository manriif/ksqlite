@file:Suppress("DEPRECATION")

package ksqlite.wasm

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