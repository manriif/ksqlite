package ksqlite.capi.handlers

import ksqlite.capi.convertActionCode
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.callbacks.Sqlite3SetAuthorizerCallback

/**
 * Handler for [ksqlite.capi.sqlite3_set_authorizer].
 */
internal class SetAuthorizerHandler(manager: MemoryManager) : Handler(manager) {

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
    ): Int = handler(refPointer) { callback: Sqlite3SetAuthorizerCallback, userData ->
        callback(
            userData,
            convertActionCode(action),
            param3.toKStringFromUtf8OrNull(),
            param4.toKStringFromUtf8OrNull(),
            param5.toKStringFromUtf8OrNull(),
            param6.toKStringFromUtf8OrNull()
        ).code
    }
}