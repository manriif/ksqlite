package ksqlite.capi.handlers

import ksqlite.capi.convertActionCode
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.callbacks.Sqlite3AuthorizerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
internal class AuthorizerHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer,
        action: Int,
        param3: WasmPointer,
        param4: WasmPointer,
        param5: WasmPointer,
        param6: WasmPointer
    ): Int = handler(refPointer) { callback: Sqlite3AuthorizerCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            action = convertActionCode(action),
            param3 = param3.toKStringFromUtf8OrNull(),
            param4 = param4.toKStringFromUtf8OrNull(),
            param5 = param5.toKStringFromUtf8OrNull(),
            param6 = param6.toKStringFromUtf8OrNull()
        ).code
    }
}