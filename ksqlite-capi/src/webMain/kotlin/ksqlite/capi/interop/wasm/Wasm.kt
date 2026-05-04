package ksqlite.capi.interop.wasm

import ksqlite.capi.interop.sqlite3
import kotlin.js.JsBigInt

internal typealias WasmPointer = JsBigInt

/**
 * Wasm null pointer.
 */
internal val NullPtr: WasmPointer
    get() = sqlite3.wasm.ptr.`null`