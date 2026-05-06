package ksqlite.capi.interop

import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmMemory
import ksqlite.capi.interop.wasm.WasmPtr
import kotlin.js.JsAny

/**
 * The [sqlite3.wasm](https://sqlite.org/wasm/doc/trunk/api-wasm.md) namespace, abbreviated as wasm
 * for the remainder of this page, holds a number of routines for working with WASM-side constructs.
 * They include APIs for such tasks as...
 *
 * - Memory management.
 *     - Allocating and freeing memory.
 *     - Helpers for working with WASM heap memory, e.g. getting and setting primitive values
 *     from/to the WASM heap.
 * - Configurable result value and argument type conversion for WASM-exported functions.
 * - JS/C String conversions.
 * - Binding JS functions into the WASM runtime, so that they may be called from WASM code (i.e.
 * from C).
 *
 * In short, if a WASM-specific feature has been needed during the development of the sqlite3 JS API,
 * it's been added to this namespace. For the most part, high-level client code will rarely need to
 * make use of more than a few of these, whereas clients using the C-style APIs may make heavy use
 * of them.
 */
internal external interface Sqlite3Wasm : JsAny, WasmFunctions, WasmMemory {

    /**
     * Wasm exports namespace.
     */
    val exports: Sqlite3WasmExports

    /**
     * [WasmPtr] instance.
     */
    val ptr: WasmPtr
}