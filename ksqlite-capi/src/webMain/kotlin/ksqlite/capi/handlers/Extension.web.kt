package ksqlite.capi.handlers

import ksqlite.capi.autoExtensionHandle
import ksqlite.capi.exports
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.StaticMemoryManager
import ksqlite.capi.memory.allocateUtf8Pointer
import ksqlite.capi.memory.heapScoped
import ksqlite.capi.memory.isNull
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines

/**
 * Singleton handler for auto extensions.
 */
internal val SharedAutoExtensionHandler by lazy {
    StaticMemoryManager.functionPointer(::AutoExtensionHandler)
}

/**
 * Handler for [ksqlite.capi.sqlite3_auto_extension].
 */
internal class AutoExtensionHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
        ),
        function = this::apply
    )

    private fun apply(
        db: WasmPointer,
        pzErrMsg: WasmPointer,
        pApi: WasmPointer
    ): Int = autoExtensionHandle(
        db = sqlite3(db),
        api = sqlite3_api_routines(pApi),
        errorPointer = pzErrMsg.takeUnless(WasmPointer::isNull)
    ) { errorPointer, message ->
        heapScoped {
            memory.pokePtr(
                errorPointer,
                exports.sqlite3_mprintf(message.allocateUtf8Pointer(), NullPtr)
            )
        }
    }
}