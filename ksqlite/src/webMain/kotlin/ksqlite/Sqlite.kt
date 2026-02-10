package ksqlite

import kotlin.js.JsAny

internal external interface Capi: JsAny {

    fun sqlite3_libversion(): String
}

internal external interface Sqlite: JsAny {

    val capi: Capi
}