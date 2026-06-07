package ksqlite.capi.handlers

import ksqlite.capi.callbacks.Sqlite3CollationCompareCallback
import ksqlite.capi.callbacks.Sqlite3CollationNeededCallback
import ksqlite.capi.convertTextEncoding
import ksqlite.wasm.FunctionSignature
import ksqlite.wasm.WasmFunctions
import ksqlite.wasm.WasmPointer
import ksqlite.wasm.installFunction
import ksqlite.capi.memory.readByteArray
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
internal class CollationCompareHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
        ),
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer,
        size1: Int,
        text1: WasmPointer,
        size2: Int,
        text2: WasmPointer
    ): Int = handle(refPointer) { callback: Sqlite3CollationCompareCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            lhs = text1.readByteArray(size1),
            rhs = text2.readByteArray(size2)
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_collation_needed].
 */
internal class CollationNeededHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer
        ),
        function = this::apply
    )

    private fun apply(
        refPointer: WasmPointer,
        db: WasmPointer,
        eTextRep: Int,
        name: WasmPointer
    ): Unit = handle(refPointer) { callback: Sqlite3CollationNeededCallback<Any?>, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            eTextRep = convertTextEncoding(eTextRep),
            name = name.toKStringFromUtf8()
        )
    }
}