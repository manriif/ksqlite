package ksqlite.capi.interop.wasm

import kotlin.js.JsBigInt

/**
 * C-style string pointer and its associated size.
 */
internal data class CString(
    val pointer: WasmPointer,
    val size: JsBigInt
)