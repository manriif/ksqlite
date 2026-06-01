package ksqlite.capi.handlers

import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.toArray
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.memory.toKStringFromUtf8OrNull
import ksqlite.capi.callbacks.Sqlite3ExecCallback

/**
 * Handler for [ksqlite.capi.sqlite3_exec].
 */
internal class ExecHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer,
        columnCount: Int,
        values: WasmPointer,
        names: WasmPointer
    ): Int = handle(refPointer) { callback: Sqlite3ExecCallback<Any?>, appData ->
        val columnValues = values.toArray(columnCount) { it.toKStringFromUtf8OrNull() }
        val columnNames = names.toArray(columnCount) { it.toKStringFromUtf8() }

        callback.apply(
            appData = appData,
            columnCount = columnCount,
            columnValues = columnValues,
            columnNames = columnNames
        )
    }
}