package ksqlite.capi.handlers

import ksqlite.capi.convertActionCode
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.callbacks.Sqlite3PreupdateHookCallback
import ksqlite.capi.callbacks.Sqlite3UpdateHookCallback
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
internal class PreupdateHookHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int64,
            FunctionSignature.Int64,
        ),
        function = ::handle
    )

    fun handle(
        refPointer: WasmPointer,
        db: WasmPointer,
        action: Int,
        dbName: WasmPointer,
        tableName: WasmPointer,
        iKey1: Long,
        iKey2: Long
    ): Unit = handler(refPointer) { callback: Sqlite3PreupdateHookCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            db = sqlite3(db),
            action = convertActionCode(action),
            dbName = dbName.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            preRowId = iKey1,
            postRowId = iKey2
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
internal class UpdateHookHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int64,
        ),
        function = ::handle
    )

    fun handle(
        refPointer: WasmPointer,
        action: Int,
        dbName: WasmPointer,
        tableName: WasmPointer,
        rowId: Long
    ): Unit = handler(refPointer) { callback: Sqlite3UpdateHookCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            action = convertActionCode(action),
            dbName = dbName.toKStringFromUtf8(),
            tableName = tableName.toKStringFromUtf8(),
            rowId = rowId
        )
    }
}