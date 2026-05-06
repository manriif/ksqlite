@file:Suppress("DEPRECATION")

package ksqlite.capi.interop.wasm

import kotlin.js.JsAny
import kotlin.js.nativeInvoke

/**
 * Wasm alloc function.
 */
internal external interface WasmAlloc : JsAny {

    /**
     * Invokes the base function, which can throws.
     */
    @nativeInvoke
    operator fun invoke(n: Int): WasmPointer

    /**
     * Invokes the impl function, which does not throw.
     */
    fun impl(n: Int): WasmPointer
}

/**
 * Wasm realloc function.
 */
internal external interface WasmRealloc : JsAny {

    /**
     * Invokes the base function, which can throws.
     */
    @nativeInvoke
    operator fun invoke(ptr: WasmPointer, size: Int): WasmPointer

    /**
     * Invokes the impl function, which does not throw.
     */
    fun impl(ptr: WasmPointer, size: Int): WasmPointer
}