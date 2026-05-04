@file:OptIn(ExperimentalWasmJsInterop::class)

package ksqlite.capi

import kotlinx.coroutines.awaitCancellation
import ksqlite.capi.interop.sqlite3
import ksqlite.capi.utils.toKStringFromUtf8
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.JsArray

@JsFun("""(args) => console.log(...args)""")
private external fun log(args: JsArray<out JsAny?>)

@JsFun("""(arg) => console.log(typeof arg)""")
private external fun typeOf(arg: JsAny)

internal actual suspend fun initializeSqliteForSynchronousTest() {
    initializeSqlite(/*debugModule = ::log*/)
    val version = sqlite3.wasm.exports.sqlite3_libversion()
    val utf8 = version.toKStringFromUtf8()
    val bytes = utf8.encodeToByteArray()

    println(sqlite3.wasm.ptr.`null`)
    //log(arrayOf(sqlite3.wasm.exports.sqlite3_libversion().toKStringFromUtf8()).toJsArray())
    awaitCancellation()
}