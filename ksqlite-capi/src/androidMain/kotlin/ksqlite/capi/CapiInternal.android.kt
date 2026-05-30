package ksqlite.capi

import ksqlite.OutputPointer
import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.handlers.destructorHandler
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.orNull
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value
import ksqlite.sqlite3_aggregate_context as jni_sqlite3_aggregate_context
import ksqlite.sqlite3_column_buffer as jni_sqlite3_column_buffer
import ksqlite.sqlite3_get_auxdata as jni_sqlite3_get_auxdata
import ksqlite.sqlite3_set_auxdata as jni_sqlite3_set_auxdata
import ksqlite.sqlite3_user_data as jni_sqlite3_user_data
import ksqlite.sqlite3_value_buffer as jni_sqlite3_value_buffer
import ksqlite.sqlite3_value_pointer as jni_sqlite3_value_pointer

private inline fun toBuffer(block: (OutputPointer.OfInt64) -> Long): Buffer? {
    val size = OutputPointer.OfInt64()
    val pointer = block(size)
    return Buffer.from(pointer, size.value)
}

internal actual fun columnBufferInternal(
    stmt: sqlite3_stmt,
    index: Int
): Buffer? = toBuffer { jni_sqlite3_column_buffer(stmt.pointer, index, it) }

internal actual fun valueBufferInternal(value: sqlite3_value): Buffer? =
    toBuffer { jni_sqlite3_value_buffer(value.pointer, it) }

internal actual fun aggregateContextInternal(
    context: sqlite3_context,
    create: Boolean
): Long? = jni_sqlite3_aggregate_context(context.pointer, create).orNull

internal actual fun getAuxdataInternal(context: sqlite3_context, index: Int): Long? =
    jni_sqlite3_get_auxdata(context.pointer, index).orNull

internal actual fun setAuxdataInternal(
    context: sqlite3_context,
    index: Int,
    destroy: Sqlite3DestroyCallback<Nothing?>
): Long? = jni_sqlite3_set_auxdata(context.pointer, index, destructorHandler(null, destroy)).orNull

@PublishedApi
internal actual fun userDataInternal(context: sqlite3_context): ApplicationDefinedFunction<*>? =
    jni_sqlite3_user_data(context.pointer) as? ApplicationDefinedFunction<*>

@PublishedApi
internal actual fun valuePointerInternal(
    value: sqlite3_value,
    type: String?
): Any? = jni_sqlite3_value_pointer(value.pointer, type)