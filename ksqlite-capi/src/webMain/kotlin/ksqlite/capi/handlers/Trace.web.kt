/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_stmt
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.JsReference
import kotlin.js.toJsReference
import kotlin.js.toLong

@JsFun("(jsRef, handler) => (p0, p1, p2, p3) => handler(jsRef, p0, p1, p2, p3)")
private external fun trace(
    jsRef: JsReference<TraceHandler>,
    handler: (
        jsRef: JsReference<TraceHandler>,
        code: Int,
        refPointer: WasmPointer,
        pPointer: WasmPointer,
        xPointer: WasmPointer
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
internal class TraceHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = trace(toJsReference()) { jsRef, code, refPointer, pPointer, xPointer ->
            jsRef.handle(refPointer) { callback: SqliteTraceCallback<Any?>, appData ->
                dispatchTraceEvent(
                    callback = callback,
                    appData = appData,
                    code = code,
                    pPointer = pPointer,
                    xPointer = xPointer,
                    toDb = ::sqlite3,
                    toStatement = ::sqlite3_stmt,
                    toString = { it.toKStringFromUtf8() },
                    toLong = { manager.memory.peek64(it).toLong() }
                )
            }
        }
    )
}