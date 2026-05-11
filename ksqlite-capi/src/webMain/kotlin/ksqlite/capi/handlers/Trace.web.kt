package ksqlite.capi.handlers

import ksqlite.capi.dispatchTraceEvent
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.Sqlite3TraceCallback
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
        pointer1: WasmPointer,
        pointer2: WasmPointer
    ): Int = handler(refPointer) { callback: Sqlite3TraceCallback, userData ->
        dispatchTraceEvent(
            callback = callback,
            userData = userData,
            code = code,
            pointer1 = pointer1,
            pointer2 = pointer2,
            toDb = ::sqlite3,
            toStatement = ::sqlite3_stmt,
            toString = { it.toKStringFromUtf8() },
            toLong = { memory.peek64(it).toLong() }
        )
    }
}