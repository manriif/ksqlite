@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.capi.interop.wasm.Sqlite3WasmExports
import ksqlite.capi.types.sqlite3

private inline val exports: Sqlite3WasmExports
    get() = ksqlite.capi.interop.sqlite3.wasm.exports

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

public actual fun sqlite3_libversion(): String =
    exports.sqlite3_libversion().toString()

public actual fun sqlite3_libversion_number(db: sqlite3): Int =
    exports.sqlite3_libversion_number()