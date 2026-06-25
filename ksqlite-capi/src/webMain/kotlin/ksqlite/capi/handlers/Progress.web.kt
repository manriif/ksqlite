package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteProgressHandlerCallback
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.JsReference
import kotlin.js.toJsReference

@JsFun("(jsRef, handler) => (p0) => handler(jsRef, p0)")
private external fun progressHandler(
    jsRef: JsReference<ProgressHandlerHandler>,
    handler: (
        jsRef: JsReference<ProgressHandlerHandler>,
        refPointer: WasmPointer
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_progress_handler].
 */
internal class ProgressHandlerHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(FunctionSignature.Pointer),
        function = progressHandler(toJsReference()) { jsRef, refPointer ->
            jsRef.handle(refPointer) { callback: SqliteProgressHandlerCallback<Any?>, appData ->
                callback.apply(appData)
            }
        }
    )
}