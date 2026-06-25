package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteBusyHandlerCallback
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.JsReference
import kotlin.js.toJsReference

@JsFun("(jsRef, handler) => (p0, p1) => handler(jsRef, p0, p1)")
private external fun busyHandler(
    jsRef: JsReference<BusyHandlerHandler>,
    handler: (
        jsRef: JsReference<BusyHandlerHandler>,
        refPointer: WasmPointer,
        count: Int
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_busy_handler].
 */
internal class BusyHandlerHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
        ),
        function = busyHandler(toJsReference()) { jsRef, refPointer, count ->
            jsRef.handle(refPointer) { callback: SqliteBusyHandlerCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    count = count
                )
            }
        }
    )
}