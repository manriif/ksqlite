package ksqlite.capi.interop.wasm

import ksqlite.capi.interop.sqlite3
import kotlin.js.JsAny
import kotlin.js.JsBigInt
import kotlin.js.JsName

/**
 * Wasm pointer type.
 */
internal typealias WasmPointer = JsBigInt
internal typealias WasmFunction = JsAny

/**
 * The [wasm.ptr](https://sqlite.org/wasm/doc/trunk/api-wasm.md#wasm-ptr) API was added to assist in
 * smoothing over the differences between 32- and 64-bit JS/WASM environments.
 */
internal external interface Ptr {

    /**
     * A "null" pointer of type Number or BigInt. Equivalent to one of Number(0) or BigInt(0).
     *
     * This value is guaranteed to compare === to WASM NULL pointers and to compare ==0 (not ===0,
     * which only applies in 32-builds!).
     */
    @JsName("null")
    val `null`: WasmPointer
}

/**
 * Wasm null pointer.
 */
internal val NullPtr: WasmPointer
    get() = sqlite3.wasm.ptr.`null`