package ksqlite.capi

import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction
import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.setValue
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_api_routines

/**
 * Singleton handler for auto extensions.
 */
internal val SharedAutoExtensionHandler = wasm.installFunction(
    signature = FunctionSignature.Int32(
        FunctionSignature.Pointer,
        FunctionSignature.Pointer,
        FunctionSignature.Pointer,
    ),
    function = ::autoExtensionHandler
)

/**
 * Handler for [sqlite3_auto_extension].
 */
private fun autoExtensionHandler(
    db: WasmPointer,
    pzErrMsg: WasmPointer,
    pApi: WasmPointer
): Int = autoExtensionHandle(
    db = sqlite3(db),
    api = sqlite3_api_routines(pApi),
    errorPointer = pzErrMsg.takeUnless(WasmPointer::isNull)
) { errorPointer, message ->
    errorPointer.setValue(sqlite3_mprintf(message))
}