@file:OptIn(ExperimentalWasmJsInterop::class)

package ksqlite.capi

import kotlinx.coroutines.awaitCancellation
import ksqlite.capi.interop.wasm.IR
import ksqlite.capi.memory.stackScoped
import ksqlite.capi.memory.toKStringFromUtf8
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.toInt
import kotlin.js.toJsString
import kotlin.time.measureTimedValue

@JsFun("""(args) => console.log(...args)""")
private external fun log(vararg args: JsAny)

@JsFun("""(arg) => console.log(typeof arg)""")
private external fun typeOf(arg: JsAny)

internal actual suspend fun initializeSqliteForSynchronousTest() {
    initializeSqlite(/*debugModule = ::log*/)
    //log(wasm)

    val (result, duration) = measureTimedValue {
        stackScoped {
            val pointer1 = allocate(IR.I32)
            val pointer2 = allocate(IR.I32)
            val pointer3 = allocate(IR.I32)

            memory.run {
                var value1 = peek32(pointer1)
                var value2 = peek32(pointer2)
                val value3 = peek32(pointer3)

                log("initValues = $value1, $value2, $value3".toJsString())

                poke32(pointer1, 5)
                poke32(pointer2, 10)

                value1 = peek32(pointer1)
                value2 = peek32(pointer2)

                log("nextValues = $value1, $value2".toJsString())

                poke32(pointer3, value1.toInt() + value2.toInt())
                peek32(pointer3)
            }
        }
    }

    log("result (${duration.inWholeMicroseconds}) = $result".toJsString())
    awaitCancellation()
}