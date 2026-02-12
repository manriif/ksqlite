package ksqlite

import kotlin.js.JsAny

/**
 * Object constructed by the [sqliteInitModule] function.
 */
internal external interface Sqlite: JsAny {
    /**
     * The namespace for the C-style APIs.
     */
    val capi: Capi
    val oo1: JsAny
    val vtab: JsAny
    val vfs: JsAny
}