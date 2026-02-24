@file:OptIn(ExperimentalWasmJsInterop::class)

package ksqlite.capi

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray

@JsFun("(args) => console.log(\"KSQLITE: \", ...args)")
private external fun log(args: JsArray<out JsAny?>)

internal actual suspend fun initializeSqliteForSynchronousTest() {
    initializeSqlite(debugModule = ::log)
}