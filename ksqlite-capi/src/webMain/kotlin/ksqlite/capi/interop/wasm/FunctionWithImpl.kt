@file:Suppress("DEPRECATION")

package ksqlite.capi.interop.wasm

import kotlin.js.nativeInvoke

/**
 * Wasm function that can be invoked and that also exposes a [impl] function.
 */
internal external interface FunctionWithImpl<T> {

    /**
     * Invokes the base function.
     */
    @nativeInvoke
    operator fun invoke(): T

    /**
     * Invokes the impl function.
     */
    fun impl(): T
}