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
package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteExecCallback
import ksqlite.capi.memory.toNullableStringArrayOrEmpty
import ksqlite.capi.memory.toStringArrayOrEmpty
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.JsReference
import kotlin.js.toJsReference

@JsFun("(jsRef, handler) => (p0, p1, p2, p3) => handler(jsRef, p0, p1, p2, p3)")
private external fun exec(
    jsRef: JsReference<ExecHandler>,
    handler: (
        jsRef: JsReference<ExecHandler>,
        refPointer: WasmPointer,
        columnCount: Int,
        values: WasmPointer,
        names: WasmPointer
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
internal class ExecHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = exec(toJsReference()) { jsRef, refPointer, columnCount, values, names ->
            jsRef.handle(refPointer) { callback: SqliteExecCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    columnCount = columnCount,
                    columnValues = values.toNullableStringArrayOrEmpty(columnCount),
                    columnNames = names.toStringArrayOrEmpty(columnCount)
                )
            }
        }
    )
}