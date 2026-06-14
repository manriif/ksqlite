package ksqlite.capi.handlers

import ksqlite.capi.dispatchSqlLogEvent
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.callbacks.SqliteConfigLogCallback
import ksqlite.capi.callbacks.SqliteConfigSqlLogCallback
import ksqlite.capi.types.sqlite3

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
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer,
        errCode: Int,
        errMsg: WasmPointer
    ): Unit = handle(refPointer) { callback: SqliteConfigLogCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            errorCode = errCode,
            message = errMsg.toKStringFromUtf8OrNull()
        )
    }
}

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
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer,
        db: WasmPointer,
        name: WasmPointer,
        type: Int
    ): Unit = handle(refPointer) { callback: SqliteConfigSqlLogCallback<Any?>, appData ->
        dispatchSqlLogEvent(
            callback = callback,
            appData = appData,
            type = type,
            db = sqlite3(db),
            name = name.toKStringFromUtf8OrNull()
        )
    }
}