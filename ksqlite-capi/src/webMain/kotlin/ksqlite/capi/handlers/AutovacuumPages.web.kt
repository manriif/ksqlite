package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteAutovacuumPagesCallback
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import kotlin.js.JsReference
import kotlin.js.toJsReference

@JsFun("(jsRef, handler) => (p0, p1, p2, p3, p4) => handler(jsRef, p0, p1, p2, p3, p4)")
private external fun autovacuumPages(
    jsRef: JsReference<AutovacuumPagesHandler>,
    handler: (
        jsRef: JsReference<AutovacuumPagesHandler>,
        refPointer: WasmPointer,
        zSchema: WasmPointer,
        nDbPage: Int,
        nFreePage: Int,
        nBytePerPage: Int
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_autovacuum_pages].
 */
internal class AutovacuumPagesHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Int32,
            FunctionSignature.Int32,
        ),
        function = autovacuumPages(toJsReference()) { jsRef, refPointer, zSchema, nDbPage, nFreePage, nBytePerPage ->
            jsRef.handle(refPointer) { callback: SqliteAutovacuumPagesCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    schemaName = zSchema.toKStringFromUtf8(),
                    dbPage = nDbPage.toUInt(),
                    freePage = nFreePage.toUInt(),
                    bytePerPage = nBytePerPage.toUInt()
                ).toInt()
            }
        }
    )
}