package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction

/**
 * Handler for [ksqlite.capi.sqlite3_wal_hook].
 */
internal class WalHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
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
        dbName: WasmPointer,
        nPage: Int,
    ): Int = handle(refPointer) { callback: SqliteWalHookCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            databaseName = dbName.toKStringFromUtf8(),
            pageCount = nPage
        ).code
    }
}