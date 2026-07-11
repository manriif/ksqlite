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
 * Returns the size in bytes without the null character.
 */
public val CString.contentSize: Int
    inline get() = byteLength - 1