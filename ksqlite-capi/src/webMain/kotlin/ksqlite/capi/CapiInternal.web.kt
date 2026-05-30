package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.interop.wasm.IR
import ksqlite.capi.interop.wasm.sizeofIR
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.allocateUtf8Pointer
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.heapScoped
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
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
    destroy: Sqlite3DestroyCallback<Nothing?>
): Long? = context.db.withMemoryManager {
    val pointer = stableRefPointer(null, null, destroy)
    val disposer = stableRefDisposer(null, destroy)
    exports.sqlite3_set_auxdata(context.pointer, index, pointer, disposer)
    pointer.orNull?.toLong()
}

@PublishedApi
internal actual fun userDataInternal(context: sqlite3_context): ApplicationDefinedFunction<*>? {
    val pointer = exports.sqlite3_user_data(context.pointer).orNull ?: return null
    return context.db.memory.stableRefData<ApplicationDefinedFunction<*>, Nothing?>(pointer).first
}

@PublishedApi
internal actual fun valuePointerInternal(
    value: sqlite3_value,
    type: String?
): Any? = heapScoped {
    val pointer = exports.sqlite3_value_pointer(value.pointer, type.allocateUtf8Pointer()).orNull
        ?: return null

    globalMemory.stableRefData<NamedPointer<*>, Any?>(pointer).second
}