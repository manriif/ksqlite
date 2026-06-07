package ksqlite.capi

import ksqlite.wasm.FunctionSignature
import ksqlite.wasm.WasmPointer
import ksqlite.wasm.installFunction
import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.setPointerValue
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
    errorPointer.setPointerValue(sqlite3_mprintf(message))
}