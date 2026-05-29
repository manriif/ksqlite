package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.handlers.destructorHandler
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.orNull
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3_aggregate_context as jni_sqlite3_aggregate_context
import ksqlite.sqlite3_get_auxdata as jni_sqlite3_get_auxdata
import ksqlite.sqlite3_set_auxdata as jni_sqlite3_set_auxdata

internal actual fun nativeAggregateContext(
    context: sqlite3_context,
    create: Boolean
): Long? = jni_sqlite3_aggregate_context(context.pointer, create).orNull

internal actual fun nativeGetAuxdata(context: sqlite3_context, index: Int): Long? =
    jni_sqlite3_get_auxdata(context.pointer, index).orNull

internal actual fun nativeSetAuxdata(
    context: sqlite3_context,
    index: Int,
    destroy: Sqlite3DestroyCallback<Nothing?>
): Long? = jni_sqlite3_set_auxdata(context.pointer, index, destructorHandler(null, destroy)).orNull

@PublishedApi
internal actual fun nativeUserData(context: sqlite3_context): ApplicationDefinedFunction<*>? {
    val pointer = sqlite3.sqlite3_user_data(context.pointer).orNull ?: return null
    return context.db.memory.stableRefData<ApplicationDefinedFunction<*>, Nothing?>(pointer).first
}

@PublishedApi
internal actual fun nativeValuePointer(
    value: sqlite3_value,
    type: String?
): Any? = memScoped {
    val pointer = sqlite3.sqlite3_value_pointer(value.pointer, type.allocateUtf8()).orNull
        ?: return null

    globalMemory.stableRefData<NamedPointer<*>, Any?>(pointer).second
}