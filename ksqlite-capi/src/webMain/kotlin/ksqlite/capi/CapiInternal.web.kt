@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.foreign.Sqlite3WasmExports
import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.HeapAllocatorScope
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.allocateUtf8Pointer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.heapScoped
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.stableRefAppData
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.foreign.wasm.IR
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.sizeofIR
import kotlin.js.toLong

private val pointerSize = wasm.sizeofIR(IR.Ptr)

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