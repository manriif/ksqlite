package ksqlite.capi.handlers

import ksqlite.capi.convertTextEncoding
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.callbacks.Sqlite3CollationCompareCallback
import ksqlite.capi.memory.readByteArray
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
internal class CollationCompareHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
        ),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer,
        size1: Int,
        text1: WasmPointer,
        size2: Int,
        text2: WasmPointer
    ): Int = handler(refPointer) { callback: Sqlite3CollationCompareCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            lhs = text1.readByteArray(size1),
            rhs = text2.readByteArray(size2)
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_collation_needed].
 */
internal class CollationNeededHandler(manager: MemoryManager) : Handler(manager) {

    override fun WasmFunctions.install(): WasmPointer = installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer
        ),
        function = ::handle
    )

    private fun handle(
        refPointer: WasmPointer,
        db: WasmPointer,
        eTextRep: Int,
        name: WasmPointer
    ): Unit = handler(refPointer) { callback: Sqlite3CollationNeededCallback<Any?>, appData ->
        callback.handle(
            appData = appData,
            db = sqlite3(db),
            eTextRep = convertTextEncoding(eTextRep),
            name = name.toKStringFromUtf8()
        )
    }
}