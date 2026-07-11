package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteCollationCallback
import ksqlite.capi.callbacks.SqliteCollationNeededCallback
import ksqlite.capi.memory.readByteArray
import ksqlite.capi.memory.toKStringFromUtf8
import ksqlite.capi.sqlite3
import ksqlite.foreign.wasm.FunctionSignature
import ksqlite.foreign.wasm.JsFunction
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.installFunction
import ksqlite.types.internal.convertTextEncoding
import kotlin.js.JsReference
import kotlin.js.toJsReference

///////////////////////////////////////////////////////////////////////////
// Collation
///////////////////////////////////////////////////////////////////////////

@JsFun("(jsRef, handler) => (p0, p1, p2, p3, p4) => handler(jsRef, p0, p1, p2, p3, p4)")
private external fun collation(
    jsRef: JsReference<CollationHandler>,
    handler: (
        jsRef: JsReference<CollationHandler>,
        refPointer: WasmPointer,
        size1: Int,
        text1: WasmPointer,
        size2: Int,
        text2: WasmPointer
    ) -> Int
): JsFunction

/**
 * Handler for [ksqlite.capi.sqlite3_create_collation] and
 * [ksqlite.capi.sqlite3_create_collation_v2].
 */
internal class CollationHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Int32(
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
            FunctionSignature.Int32,
            FunctionSignature.Pointer,
        ),
        function = collation(toJsReference()) { jsRef, refPointer, size1, text1, size2, text2 ->
            jsRef.handle(refPointer) { callback: SqliteCollationCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    lhs = text1.readByteArray(size1),
                    rhs = text2.readByteArray(size2)
                )
            }
        }
    )
}

///////////////////////////////////////////////////////////////////////////
// Needed
///////////////////////////////////////////////////////////////////////////

@JsFun("(jsRef, handler) => (p0, p1, p2, p3) => handler(jsRef, p0, p1, p2, p3)")
private external fun collationNeeded(
    jsRef: JsReference<CollationNeededHandler>,
    handler: (
        jsRef: JsReference<CollationNeededHandler>,
        refPointer: WasmPointer,
        db: WasmPointer,
        eTextRep: Int,
        name: WasmPointer
    ) -> Unit
): JsFunction

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
        function = collationNeeded(toJsReference()) { jsRef, refPointer, db, eTextRep, name ->
            jsRef.handle(refPointer) { callback: SqliteCollationNeededCallback<Any?>, appData ->
                callback.apply(
                    appData = appData,
                    db = sqlite3(db),
                    eTextRep = convertTextEncoding(eTextRep),
                    name = name.toKStringFromUtf8()
                )
            }
        }
    )
}