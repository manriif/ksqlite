package ksqlite.capi

import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toLong
import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3_aggregate_context as native_sqlite3_aggregate_context
import ksqlite.sqlite3_get_auxdata as native_sqlite3_get_auxdata
import ksqlite.sqlite3_set_auxdata as native_sqlite3_set_auxdata
import ksqlite.sqlite3_user_data as native_sqlite3_user_data
import ksqlite.sqlite3_value_pointer as native_sqlite3_value_pointer

private val pointerSize = sizeOf<CPointerVarOf<*>>().toInt()

internal actual fun nativeAggregateContext(
    context: sqlite3_context,
    create: Boolean
): Long? {
    val pointer = if (create) {
        native_sqlite3_aggregate_context(context.pointer, pointerSize)
    } else {
        native_sqlite3_aggregate_context(context.pointer, 0)
    }

    return pointer?.toLong()
}

internal actual fun nativeGetAuxdata(context: sqlite3_context, index: Int): Long? {
    return native_sqlite3_get_auxdata(context.pointer, index)?.toLong()
}

internal actual fun nativeSetAuxdata(
    context: sqlite3_context,
    index: Int,
    destroy: Sqlite3DestroyCallback<Nothing?>
): Long? = context.db.withMemoryManager {
    val pointer = stableRefPointer(null, null, destroy)
    val disposer = stableRefDisposer(null, destroy)
    native_sqlite3_set_auxdata(context.pointer, index, pointer, disposer)
    pointer?.toLong()
}

@PublishedApi
internal actual fun nativeUserData(context: sqlite3_context): ApplicationDefinedFunction<*> {
    val pointer = native_sqlite3_user_data(context.pointer)
    return stableRefData<ApplicationDefinedFunction<*>, Nothing?>(pointer).first
}

@PublishedApi
internal actual fun nativeValuePointer(
    value: sqlite3_value,
    type: String?
): Any? {
    val pointer = native_sqlite3_value_pointer(value.pointer, type) ?: return null
    return stableRefData<NamedPointer<*>, Any?>(pointer).second
}