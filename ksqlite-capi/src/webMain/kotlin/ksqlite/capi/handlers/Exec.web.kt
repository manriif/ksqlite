package ksqlite.capi.handlers

import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toArray
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.types.Sqlite3ExecCallback

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
internal class ExecHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer,
        columnCount: Int,
        values: WasmPointer,
        names: WasmPointer
    ): Int = handler(refPointer) { callback: Sqlite3ExecCallback, userData ->
        val columnValues = values.toArray(columnCount) { it.toKStringFromUtf8OrNull() }
        val columnNames = names.toArray(columnCount) { it.toKStringFromUtf8() }

        callback(
            userData,
            columnCount,
            columnValues,
            columnNames
        )
    }
}