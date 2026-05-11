package ksqlite.capi.interop.wasm

import kotlin.js.JsBigInt
import kotlin.js.toLong

/**
 * C-style string pointer and its associated size.
 */
internal data class CString(
    val pointer: WasmPointer,
    val byteLength: JsBigInt
)

///////////////////////////////////////////////////////////////////////////
// Extension
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the size in bytes of the string as [Int].
 */
internal val CString.size: Int
    get() = byteLength.toLong().toInt()