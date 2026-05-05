@file:OptIn(ExperimentalWasmJsInterop::class)

package ksqlite.capi

import kotlinx.coroutines.awaitCancellation
import ksqlite.capi.interop.wasm.JsFunction
import ksqlite.capi.utils.jsArrayOf
import ksqlite.capi.utils.toKStringFromUtf8
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.toJsString

@JsFun("""(args) => console.log(...args)""")
private external fun log(vararg args: JsAny)

@JsFun("""(arg) => console.log(typeof arg)""")
private external fun typeOf(arg: JsAny)

internal actual suspend fun initializeSqliteForSynchronousTest() {
    initializeSqlite(/*debugModule = ::log*/)
    log(sqlite3.wasm)

    val jsFunction = { p0: JsAny, p1: JsAny ->
        sqlite3.wasm.exports.sqlite3_libversion().toKStringFromUtf8().toJsString()
    }

    val function = sqlite3.wasm.installFunction("ii", jsFunction as JsFunction)

    log(sqlite3.wasm.functionTable())
    awaitCancellation()
}