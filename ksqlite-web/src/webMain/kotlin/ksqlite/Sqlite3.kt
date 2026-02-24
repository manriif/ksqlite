package ksqlite

import kotlin.js.JsAny

/**
 * Object constructed by the [sqliteInitModule] function.
 */
public external interface Sqlite3 : JsAny {
    /**
     * The namespace for the C-style APIs.
     */
    public val capi: Capi
    public val wasm: Wasm
    public val oo1: JsAny
    public val vtab: JsAny
    public val vfs: JsAny
}