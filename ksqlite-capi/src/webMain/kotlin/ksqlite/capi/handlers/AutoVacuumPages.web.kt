package ksqlite.capi.handlers

import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.callbacks.Sqlite3AutoVacuumPagesCallback

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutoVacuumPagesHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Int32,
            FunctionSignature.Int32,
        ),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer,
        zSchema: WasmPointer,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ): Int = handler(refPointer) { callback: Sqlite3AutoVacuumPagesCallback, userData ->
        callback(
            userData,
            zSchema.toKStringFromUtf8(),
            nDbPage.toUInt(),
            nFreePage.toUInt(),
            nBytePerPage.toUInt()
        ).toInt()
    }
}