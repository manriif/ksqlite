@file:Suppress("FunctionName", "SpellCheckingInspection", "REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.capi

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.HeapAllocatorScope
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.allocateUtf8Pointer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.heapScoped
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.stableRefAppData
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.useParam
import ksqlite.capi.memory.withMemoryManager
import ksqlite.foreign.Sqlite3Capi
import ksqlite.foreign.Sqlite3Wasm
import ksqlite.foreign.Sqlite3WasmExports
import ksqlite.foreign.sqlite3
import ksqlite.foreign.wasm.IR
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.sizeofIR
import ksqlite.types.SqliteSerializeFlag
import kotlin.js.JsBigInt
import kotlin.js.toJsBigInt
import kotlin.js.toLong

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

private val pointerSize = wasm.sizeofIR(IR.Ptr)

internal val sqliteTransient: JsBigInt by lazy {
    sqlite3.capi.SQLITE_TRANSIENT.toLong().toJsBigInt()
}

///////////////////////////////////////////////////////////////////////////
// Shortucts
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the [Sqlite3Capi] instance.
 */
internal inline val capi: Sqlite3Capi
    get() = sqlite3.capi

/**
 * Returns the [Sqlite3Wasm] instance.
 */
internal inline val wasm: Sqlite3Wasm
    get() = sqlite3.wasm

/**
 * Returns the [Sqlite3WasmExports] instance.
 */
internal inline val exports: Sqlite3WasmExports
    get() = wasm.exports

///////////////////////////////////////////////////////////////////////////
// Custom handling
///////////////////////////////////////////////////////////////////////////

internal actual fun columnBufferInternal(
    stmt: sqlite3_stmt,
    index: Int
): Buffer? = commonColumnBuffer(
    stmt = stmt,
    index = index,
    pointer = exports.sqlite3_column_blob(stmt.pointer, index),
    toBuffer = Buffer::from
)

internal actual fun valueBufferInternal(value: sqlite3_value): Buffer? = commonValueBuffer(
    value = value,
    pointer = exports.sqlite3_value_blob(value.pointer),
    toBuffer = Buffer::from
)

internal actual fun aggregateContextInternal(
    context: sqlite3_context,
    create: Boolean
): Long? {
    val pointer = if (create) {
        exports.sqlite3_aggregate_context(context.pointer, pointerSize)
    } else {
        exports.sqlite3_aggregate_context(context.pointer, 0)
    }

    return pointer.orNull?.toLong()
}

internal actual fun getAuxdataInternal(context: sqlite3_context, index: Int): Long? {
    return exports.sqlite3_get_auxdata(context.pointer, index).orNull?.toLong()
}

internal actual fun setAuxdataInternal(
    context: sqlite3_context,
    index: Int,
    destroy: SqliteDestroyCallback<Nothing?>
): Long? = context.db.withMemoryManager {
    val pointer = stableRefPointer(null, null, destroy)
    val disposer = stableRefDisposer(null, destroy)
    exports.sqlite3_set_auxdata(context.pointer, index, pointer, disposer)
    pointer.orNull?.toLong()
}

@PublishedApi
internal actual fun userDataInternal(context: sqlite3_context): ApplicationDefinedFunction<*>? {
    val pointer = exports.sqlite3_user_data(context.pointer).orNull ?: return null
    return context.db.memory.stableRefData<ApplicationDefinedFunction<*>>(pointer)
}

public actual fun serializeInternal(
    db: sqlite3,
    database: String?,
    outSize: Int64OutputParam,
    flags: SqliteSerializeFlag?
): Buffer? = Buffer.from(
    pointer = heapScoped {
        useParam(outSize) { sizePtr ->
            val mFlags = flags?.value ?: 0
            exports.sqlite3_serialize(db.pointer, database.allocateUtf8Pointer(), sizePtr, mFlags)
        }
    },
    size = outSize.value
)

@PublishedApi
internal actual fun valuePointerInternal(
    value: sqlite3_value,
    type: String?
): Any? = heapScoped {
    val pointer = exports.sqlite3_value_pointer(value.pointer, type.allocateUtf8Pointer()).orNull
        ?: return null

    globalMemory.stableRefAppData(pointer)
}

///////////////////////////////////////////////////////////////////////////
// Utils
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a pointer to a string holding [text]'s content, obtained through
 * [Sqlite3WasmExports.sqlite3_mprintf].
 */
context(allocator: HeapAllocatorScope)
internal fun sqlite3_mprintf(text: String): WasmPointer =
    exports.sqlite3_mprintf(text.allocateUtf8Pointer(), NullPtr)

/**
 * Returns a pointer to a string holding [text]'s content, obtained through
 * [Sqlite3WasmExports.sqlite3_mprintf]. Returns [NullPtr] if [text] is `null`.
 */
internal fun sqlite3_mprintf(text: String?): WasmPointer {
    if (text == null) {
        return NullPtr
    }

    return heapScoped {
        sqlite3_mprintf(text)
    }
}