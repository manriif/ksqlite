package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteRollbackHookCallback
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.JsReference
import kotlin.js.toJsReference

///////////////////////////////////////////////////////////////////////////
// Commit
///////////////////////////////////////////////////////////////////////////

@JsFun("(jsRef, handler) => (p0) => handler(jsRef, p0)")
private external fun commitHook(
    jsRef: JsReference<CommitHookHandler>,
    handler: (
        jsRef: JsReference<CommitHookHandler>,
        refPointer: WasmPointer
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(FunctionSignature.Pointer),
        function = commitHook(toJsReference()) { jsRef, refPointer ->
            jsRef.handle(refPointer) { callback: SqliteCommitHookCallback<Any?>, appData ->
                callback.apply(appData)
            }
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// Rollback
///////////////////////////////////////////////////////////////////////////

@JsFun("(jsRef, handler) => (p0) => handler(jsRef, p0)")
private external fun rollbackHook(
    jsRef: JsReference<RollbackHookHandler>,
    handler: (
        jsRef: JsReference<RollbackHookHandler>,
        refPointer: WasmPointer
    ) -> Unit
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = rollbackHook(toJsReference()) { jsRef, refPointer ->
            jsRef.handle(refPointer) { callback: SqliteRollbackHookCallback<Any?>, appData ->
                callback.apply(appData)
            }
        }
    )
}