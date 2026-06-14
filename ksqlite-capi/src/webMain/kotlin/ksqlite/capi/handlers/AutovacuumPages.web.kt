package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3AutovacuumPagesCallback
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.capi.memory.toKStringFromUtf8

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutovacuumPagesHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Int32,
            FunctionSignature.Int32,
        ),
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer,
        zSchema: WasmPointer,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int = handle(refPointer) { callback: Sqlite3AutovacuumPagesCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            schemaName = zSchema.toKStringFromUtf8(),
            dbPage = nDbPage.toUInt(),
            freePage = nFreePage.toUInt(),
            bytePerPage = nBytePerPage.toUInt()
        ).toInt()
    }
}