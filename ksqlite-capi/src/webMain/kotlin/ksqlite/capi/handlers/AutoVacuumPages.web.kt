package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.toKStringFromUtf8

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutoVacuumPagesHandler : Handler() {

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
    ): Int = handle(refPointer) { callback: Sqlite3AutoVacuumPagesCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            schemaName = zSchema.toKStringFromUtf8(),
            dbPage = nDbPage.toUInt(),
            freePage = nFreePage.toUInt(),
            bytePerPage = nBytePerPage.toUInt()
        ).toInt()
    }
}