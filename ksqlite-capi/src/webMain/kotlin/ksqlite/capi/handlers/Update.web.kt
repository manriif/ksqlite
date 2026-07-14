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

import ksqlite.capi.callbacks.SqlitePreupdateHookCallback
import ksqlite.capi.callbacks.SqliteUpdateHookCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.types.internal.convertActionCode
import kotlin.js.JsReference
import kotlin.js.toJsReference

///////////////////////////////////////////////////////////////////////////
// Preupdate
///////////////////////////////////////////////////////////////////////////

@JsFun("(jsRef, handler) => (p0, p1, p2, p3, p4, p5, p6) => handler(jsRef, p0, p1, p2, p3, p4, p5, p6)")
private external fun preupdateHook(
    jsRef: JsReference<PreupdateHookHandler>,
    handler: (
        jsRef: JsReference<PreupdateHookHandler>,
        refPointer: WasmPointer,
        db: WasmPointer,
        action: Int,
        dbName: WasmPointer,
        tableName: WasmPointer,
        iKey1: Long,
        iKey2: Long
    ) -> Unit
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
internal class PreupdateHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int64,
            FunctionSignature.Int64,
        ),
        function = preupdateHook(toJsReference()) { jsRef, refPointer, db, action, dbName, tableName, iKey1, iKey2 ->
            jsRef.handle(refPointer) { callback: SqlitePreupdateHookCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    db = sqlite3(db),
                    action = convertActionCode(action),
                    dbName = dbName.toKStringFromUtf8(),
                    tableName = tableName.toKStringFromUtf8(),
                    oldRowid = iKey1,
                    newRowid = iKey2
                )
            }
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// Update
///////////////////////////////////////////////////////////////////////////

@JsFun("(jsRef, handler) => (p0, p1, p2, p3, p4) => handler(jsRef, p0, p1, p2, p3, p4)")
private external fun updateHook(
    jsRef: JsReference<UpdateHookHandler>,
    handler: (
        jsRef: JsReference<UpdateHookHandler>,
        refPointer: WasmPointer,
        action: Int,
        dbName: WasmPointer,
        tableName: WasmPointer,
        rowId: Long
    ) -> Unit
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
internal class UpdateHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int64,
        ),
        function = updateHook(toJsReference()) { jsRef, refPointer, action, dbName, tableName, rowId ->
            jsRef.handle(refPointer) { callback: SqliteUpdateHookCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    action = convertActionCode(action),
                    dbName = dbName.toKStringFromUtf8(),
                    tableName = tableName.toKStringFromUtf8(),
                    rowid = rowId
                )
            }
        }
    )
}