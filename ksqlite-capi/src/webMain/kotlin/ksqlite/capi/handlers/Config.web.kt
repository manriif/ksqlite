package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.sqlite3
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.JsReference
import kotlin.js.toJsReference

///////////////////////////////////////////////////////////////////////////
// Log
///////////////////////////////////////////////////////////////////////////

@JsFun("(jsRef, handler) => (p0, p1, p2) => handler(jsRef, p0, p1, p2)")
private external fun configLog(
    jsRef: JsReference<ConfigLogHandler>,
    handler: (
        jsRef: JsReference<ConfigLogHandler>,
        refPointer: WasmPointer,
        errCode: Int,
        errMsg: WasmPointer
    ) -> Unit
): JsFunction

/**
 * Handler for the LOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigLogHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
        ),
        function = configLog(toJsReference()) { jsRef, refPointer, errCode, errMsg ->
            jsRef.handle(refPointer) { callback: SqliteConfigLogCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    errorCode = errCode,
                    message = errMsg.toKStringFromUtf8OrNull()
                )
            }
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// SqlLog
///////////////////////////////////////////////////////////////////////////

@JsFun("(jsRef, handler) => (p0, p1, p2, p3) => handler(jsRef, p0, p1, p2, p3)")
private external fun configSqlLog(
    jsRef: JsReference<ConfigSqlLogHandler>,
    handler: (
        jsRef: JsReference<ConfigSqlLogHandler>,
        refPointer: WasmPointer,
        db: WasmPointer,
        name: WasmPointer,
        type: Int
    ) -> Unit
): JsFunction

/**
 * Handler for the SQLLOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigSqlLogHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
        ),
        function = configSqlLog(toJsReference()) { jsRef, refPointer, db, name, type ->
            jsRef.handle(refPointer) { callback: SqliteConfigSqlLogCallback<Any?>, appData ->
                dispatchSqlLogEvent(
                    callback = callback,
                    appData = appData,
                    type = type,
                    db = sqlite3(db),
                    name = name.toKStringFromUtf8OrNull()
                )
            }
        }
    )
}