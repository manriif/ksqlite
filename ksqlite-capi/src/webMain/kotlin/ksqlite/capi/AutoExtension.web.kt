package ksqlite.capi

import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.setPointerValue
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction

@JsFun("(p0, p1, p2) => handler(p0, p1, p2)")
private external fun autoExtension(
    handler: (
        db: WasmPointer,
        pzErrMsg: WasmPointer,
        pApi: WasmPointer
    ) -> Int
): JsFunction

/**
 * Handler for [sqlite3_auto_extension].
 */
internal val SharedAutoExtensionHandler = wasm.installFunction(
    signature = FunctionSignature.Int32(
        FunctionSignature.Pointer,
        FunctionSignature.Pointer,
        FunctionSignature.Pointer,
    ),
    function = autoExtension { db, pzErrMsg, pApi ->
        autoExtensionHandle(
            db = sqlite3(db),
            api = pApi,
            errorPointer = pzErrMsg.takeUnless(WasmPointer::isNull)
        ) { errorPointer, message ->
            errorPointer.setPointerValue(sqlite3_mprintf(message))
        }
    }
)