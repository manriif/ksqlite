package ksqlite.capi

import ksqlite.capi.memory.isNull
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.types.sqlite3_context
import ksqlite.sqlite3
import java.lang.foreign.ValueLayout

private val pointerSize = ValueLayout.ADDRESS.byteSize().toInt()

internal actual fun nativeAggregateContext(
    context: sqlite3_context,
    create: Boolean
): Long {
    val pointer = if (create) {
        sqlite3.sqlite3_aggregate_context(context.pointer, pointerSize)
    } else {
        sqlite3.sqlite3_aggregate_context(context.pointer, 0)
    }

    return if (pointer.isNull) 0L else pointer.address()
}

@PublishedApi
internal actual fun nativeUserData(context: sqlite3_context): Udf<*> {
    return context.db.memory.stableRefData<Udf<*>, Nothing>(context.pointer).first
}