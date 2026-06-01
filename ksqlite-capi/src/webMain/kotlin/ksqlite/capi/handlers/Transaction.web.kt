package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(FunctionSignature.Pointer),
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer
    ): Int = handle(refPointer) { callback: Sqlite3CommitHookCallback<Any?>, appData ->
        callback.apply(appData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer
    ): Unit = handle(refPointer) { callback: Sqlite3RollbackHookCallback<Any?>, appData ->
        callback.apply(appData)
    }
}