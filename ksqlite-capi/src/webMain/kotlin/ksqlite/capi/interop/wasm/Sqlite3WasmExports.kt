@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi.interop.wasm

import kotlin.js.JsBigInt

/**
 * The [sqlite3.wasm.exports](https://sqlite.org/wasm/doc/trunk/api-wasm.md#wasm-exports-namespace)
 * namespace object is a WASM-standard part of the WASM module file and contains all "exported" C
 * functions which are built into the WASM module, as well as certain non-function values which are
 * part of the WASM module. The functions which live in this object are as low-level as it gets, in
 * terms of JS/C bindings2. They perform no automatic type conversions on their arguments or result
 * values and many, perhaps most, are cumbersome to use from JS because of that. This level of the
 * API is not generally recommended for client use but  is available for those who want to make use
 * of it. The functions in this object which are intended for client-side use are re-exported into
 * the sqlite3.capi namespace and have automatic type conversions applied to them (where applicable).
 * Some small handful of the functions get re-exported into the sqlite3.wasm namespace.
 */
internal external interface Sqlite3WasmExports {

    fun sqlite3_libversion(): JsBigInt

    fun sqlite3_libversion_number(): Int
}