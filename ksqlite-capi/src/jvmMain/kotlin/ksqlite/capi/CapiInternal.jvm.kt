package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.memScoped
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.withMemoryManager
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3
import java.lang.foreign.ValueLayout

private val pointerSize = ValueLayout.ADDRESS.byteSize().toInt()

internal actual fun nativeAggregateContext(
    context: sqlite3_context,
    create: Boolean
): Long? {
    val pointer = if (create) {
        sqlite3.sqlite3_aggregate_context(context.pointer, pointerSize)
    } else {
        sqlite3.sqlite3_aggregate_context(context.pointer, 0)
    }

    return pointer.orNull?.address()
}

internal actual fun nativeGetAuxdata(context: sqlite3_context, index: Int): Long? {
    return sqlite3.sqlite3_get_auxdata(context.pointer, index).orNull?.address()
}

internal actual fun nativeSetAuxdata(
    context: sqlite3_context,
    index: Int,
    destroy: Sqlite3DestroyCallback<Nothing?>
): Long? = context.db.withMemoryManager {
    val pointer = stableRefPointer(null, null, destroy)
    val disposer = stableRefDisposer(null, destroy)
    sqlite3.sqlite3_set_auxdata(context.pointer, index, pointer, disposer)
    pointer.orNull?.address()
}

@PublishedApi
internal actual fun nativeUserData(context: sqlite3_context): ApplicationDefinedFunction<*> {
    return context.db.memory
        .stableRefData<ApplicationDefinedFunction<*>, Nothing?>(context.pointer).first
}

@PublishedApi
internal actual fun nativeValuePointer(
    value: sqlite3_value,
    type: String?
): Any? = memScoped {
    val pointer = sqlite3.sqlite3_value_pointer(value.pointer, type.allocateUtf8()) ?: return null
    globalMemory.stableRefData<NamedPointer<*>, Any?>(pointer).second
}