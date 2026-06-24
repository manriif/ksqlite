@file:OptIn(ExperimentalWasmJsInterop::class)

package ksqlite.capi

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny

@JsFun("""(args) => console.log(...args)""")
private external fun log(vararg args: JsAny)

internal actual suspend fun loadSqliteForSynchronousTest(): Boolean {
    if (!isSqliteInitialized) {
        ksqliteLoad(debugModule = ::log)
    }

    return true
}