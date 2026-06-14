package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.dispatchTraceEvent
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_stmt
import kotlin.js.toLong

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
internal class TraceHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = this::apply
    )

    private fun apply(
        code: Int,
        refPointer: WasmPointer,
        pPointer: WasmPointer,
        xPointer: WasmPointer
    ): Int = handle(refPointer) { callback: SqliteTraceCallback<Any?>, appData ->
        dispatchTraceEvent(
            callback = callback,
            appData = appData,
            code = code,
            pPointer = pPointer,
            xPointer = xPointer,
            toDb = ::sqlite3,
            toStatement = ::sqlite3_stmt,
            toString = { it.toKStringFromUtf8() },
            toLong = { manager.memory.peek64(it).toLong() }
        )
    }
}