package ksqlite.foreign.wasm

import kotlin.js.JsBigInt
import kotlin.js.toLong

/**
 * C-style string pointer and its associated size.
 */
public data class CString(
    val pointer: WasmPointer,
    val byteLength: JsBigInt
)

///////////////////////////////////////////////////////////////////////////
// Extension
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the size in bytes of the string as [Int].
 */
public val CString.size: Int
    get() = byteLength.toLong().toInt()