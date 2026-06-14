package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteAuthorizerCallback
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.types.internal.convertActionCode

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
    ): Int = handle(refPointer) { callback: SqliteAuthorizerCallback<Any?>, appData ->
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