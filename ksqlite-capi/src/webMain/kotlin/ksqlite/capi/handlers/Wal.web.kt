package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteWalHookCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.types.sqlite3
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.JsReference
import kotlin.js.toJsReference

@JsFun("(jsRef, handler) => (p0, p1, p2, p3) => handler(jsRef, p0, p1, p2, p3)")
private external fun walHook(
    jsRef: JsReference<WalHookHandler>,
    handler: (
		jsRef: JsReference<WalHookHandler>,
        refPointer: WasmPointer,
        db: WasmPointer,
        dbName: WasmPointer,
        nPage: Int,
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_wal_hook].
 */
internal class WalHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
        ),
        function = walHook(toJsReference()) { jsRef, refPointer, db, dbName, nPage ->
            jsRef.handle(refPointer) { callback: SqliteWalHookCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    db = sqlite3(db),
                    databaseName = dbName.toKStringFromUtf8(),
                    pageCount = nPage
                ).code
            }
        }
    )
}