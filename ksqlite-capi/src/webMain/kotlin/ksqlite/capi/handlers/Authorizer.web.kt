package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3AuthorizerCallback
import ksqlite.capi.convertActionCode
import ksqlite.wasm.FunctionSignature
import ksqlite.wasm.WasmFunctions
import ksqlite.wasm.WasmPointer
import ksqlite.wasm.installFunction
import ksqlite.capi.memory.toKStringFromUtf8OrNull

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
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer,
        action: Int,
        param3: WasmPointer,
        param4: WasmPointer,
        param5: WasmPointer,
        param6: WasmPointer
    ): Int = handle(refPointer) { callback: Sqlite3AuthorizerCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(action),
            param3 = param3.toKStringFromUtf8OrNull(),
            param4 = param4.toKStringFromUtf8OrNull(),
            param5 = param5.toKStringFromUtf8OrNull(),
            param6 = param6.toKStringFromUtf8OrNull()
        ).code
    }
}