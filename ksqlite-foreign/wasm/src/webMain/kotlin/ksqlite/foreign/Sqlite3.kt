package ksqlite.foreign

import kotlin.js.JsAny

/**
 * Object constructed by the [sqliteInitModule] function.
 */
public external interface Sqlite3 : JsAny {

    /**
     * The namespace for the C-style APIs.
     */
    public val capi: Sqlite3Capi

    /**
     * WASM-specific utilities, abstracted to be independent of and configurable for use with,
     * arbitrary WASM runtime environments.
     */
    public val wasm: Sqlite3Wasm
}