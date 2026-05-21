package ksqlite.capi.handlers

import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.callbacks.Sqlite3CommitHookCallback
import ksqlite.capi.callbacks.Sqlite3RollbackHookCallback

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(FunctionSignature.Pointer),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer
    ): Int = handler(refPointer) { callback: Sqlite3CommitHookCallback, userData ->
        callback(userData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer
    ): Unit = handler(refPointer) { callback: Sqlite3RollbackHookCallback, userData ->
        callback(userData)
    }
}