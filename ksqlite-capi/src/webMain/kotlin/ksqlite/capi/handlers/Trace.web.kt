package ksqlite.capi.handlers

import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.callbacks.Sqlite3TraceCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_stmt
import kotlin.js.toLong

/**
 * Handler for [ksqlite.capi.sqlite3_trace_v2].
 */
internal class TraceHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = ::handle
    )

    private fun handle(
        code: Int,
        refPointer: WasmPointer,
        pPointer: WasmPointer,
        xPointer: WasmPointer
    ): Int = handler(refPointer) { callback: Sqlite3TraceCallback<Any?>, appData ->
        dispatchTraceEvent(
            callback = callback,
            appData = appData,
            code = code,
            pPointer = pPointer,
            xPointer = xPointer,
            toDb = ::sqlite3,
            toStatement = ::sqlite3_stmt,
            toString = { it.toKStringFromUtf8() },
            toLong = { memory.peek64(it).toLong() }
        )
    }
}