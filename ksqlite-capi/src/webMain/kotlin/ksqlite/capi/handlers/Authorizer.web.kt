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

import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.types.internal.convertActionCode
import kotlin.js.JsReference
import kotlin.js.toJsReference

@JsFun("(jsRef, handler) => (p0, p1, p2, p3, p4, p5) => handler(jsRef, p0, p1, p2, p3, p4, p5)")
private external fun authorizer(
    jsRef: JsReference<AuthorizerHandler>,
    handler: (
        jsRef: JsReference<AuthorizerHandler>,
        refPointer: WasmPointer,
        action: Int,
        param3: WasmPointer,
        param4: WasmPointer,
        param5: WasmPointer,
        param6: WasmPointer
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
internal class AuthorizerHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = authorizer(toJsReference()) { jsRef, refPointer, action, param3, param4, param5, param6 ->
            jsRef.handle(refPointer) { callback: SqliteAuthorizerCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    action = convertActionCode(action),
                    detail1 = param3.toKStringFromUtf8OrNull(),
                    detail2 = param4.toKStringFromUtf8OrNull(),
                    detail3 = param5.toKStringFromUtf8OrNull(),
                    detail4 = param6.toKStringFromUtf8OrNull()
                ).code
            }
        }
    )
}