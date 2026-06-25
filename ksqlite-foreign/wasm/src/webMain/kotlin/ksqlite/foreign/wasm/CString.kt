package ksqlite.foreign.wasm

/**
 * C-style string pointer and its associated size.
 */
public data class CString(
    val pointer: WasmPointer,
    val byteLength: Int
)

///////////////////////////////////////////////////////////////////////////
// Extension
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the size in bytes .
 */
public val CString.size: Int
    inline get() = byteLength