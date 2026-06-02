package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3ExecCallback
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.toNullableStringArrayOrEmpty
import ksqlite.capi.memory.toStringArrayOrEmpty

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
        callback.apply(
            appData = appData,
            columnCount = columnCount,
            columnValues = values.toNullableStringArrayOrEmpty(columnCount),
            columnNames = names.toStringArrayOrEmpty(columnCount)
        )
    }
}