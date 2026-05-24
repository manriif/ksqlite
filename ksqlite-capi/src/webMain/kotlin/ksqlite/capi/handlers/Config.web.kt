package ksqlite.capi.handlers

import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.callbacks.Sqlite3ConfigLogCallback
import ksqlite.capi.callbacks.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.types.sqlite3

/**
 * Handler for the LOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigLogHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
        ),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer,
        errCode: Int,
        errMsg: WasmPointer
    ): Unit = handler(refPointer) { callback: Sqlite3ConfigLogCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            errorCode = errCode,
            errorMsg = errMsg.toKStringFromUtf8OrNull()
        )
    }
}

/**
 * Handler for the SQLLOG option of [ksqlite.capi.sqlite3_config].
 */
internal class ConfigSqlLogHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
        ),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer,
        db: WasmPointer,
        name: WasmPointer,
        type: Int
    ): Unit = handler(refPointer) { callback: Sqlite3ConfigSqlLogCallback<Any?>, appData ->
        dispatchSqlLogEvent(
            callback = callback,
            appData = appData,
            type = type,
            db = sqlite3(db),
            name = name.toKStringFromUtf8OrNull()
        )
    }
}