package ksqlite.structs

import ksqlite.wasm.WasmPointer

/**
 * Jaccwabyt generated struct.
 */
public abstract external class JaccwabytStruct {

    /**
     * Pointer to the struct address in the WASM heap.
     */
    public val pointer: WasmPointer

    /**
     * Disposes the struct.
     */
    public fun dispose()
}