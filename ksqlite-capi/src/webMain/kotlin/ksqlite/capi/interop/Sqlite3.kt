package ksqlite.capi.interop

import ksqlite.capi.interop.Sqlite3Capi
import kotlin.js.JsAny

/**
 * Object constructed by the [ksqlite.sqliteInitModule] function.
 */
internal external interface Sqlite3 : JsAny {

    /**
     * The namespace for the C-style APIs.
     */
    val capi: Sqlite3Capi

    /**
     * WASM-specific utilities, abstracted to be independent of and configurable for use with,
     * arbitrary WASM runtime environments.
     */
    val wasm: Sqlite3Wasm
}