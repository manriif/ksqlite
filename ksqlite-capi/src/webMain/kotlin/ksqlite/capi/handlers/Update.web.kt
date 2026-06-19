package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqlitePreupdateHookCallback
import ksqlite.capi.callbacks.SqliteUpdateHookCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.types.internal.convertActionCode

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
internal class PreupdateHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int64,
            FunctionSignature.Int64,
        ),
        function = this::apply
    )

    fun apply(
        refPointer: WasmPointer,
        db: WasmPointer,
        action: Int,
        dbName: WasmPointer,
        tableName: WasmPointer,
        iKey1: Long,
        iKey2: Long
    ): Unit = handle(refPointer) { callback: SqlitePreupdateHookCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            action = convertActionCode(action),
            dbName = dbName.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            oldRowid = iKey1,
            newRowid = iKey2
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
internal class UpdateHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int64,
        ),
        function = this::apply
    )

    fun apply(
        refPointer: WasmPointer,
        action: Int,
        dbName: WasmPointer,
        tableName: WasmPointer,
        rowId: Long
    ): Unit = handle(refPointer) { callback: SqliteUpdateHookCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(action),
            dbName = dbName.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            rowid = rowId
        )
    }
}