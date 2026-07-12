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
package ksqlite.capi

import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.setPointerValue
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction

@JsFun("(handler) => (p0, p1, p2) => handler(p0, p1, p2)")
private external fun autoExtension(
    handler: (
        db: WasmPointer,
        pzErrMsg: WasmPointer,
        pApi: WasmPointer
    ) -> Int
): JsFunction

/**
 * Handler for [sqlite3_auto_extension].
 */
internal val AutoExtensionHandler = wasm.installFunction(
    signature = FunctionSignature.Int32(
        FunctionSignature.Pointer,
        FunctionSignature.Pointer,
        FunctionSignature.Pointer,
    ),
    function = autoExtension { db, pzErrMsg, pApi ->
        autoExtensionHandle(
            db = sqlite3(db),
            api = pApi,
            errorPointer = pzErrMsg.takeUnless(WasmPointer::isNull)
        ) { errorPointer, message ->
            errorPointer.setPointerValue(sqlite3_mprintf(message))
        }
    }
)