package ksqlite.capi

import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toLong
import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.stableRefAppData
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.useParam
import ksqlite.capi.memory.withMemoryManager
import ksqlite.types.SqliteSerializeFlag
import ksqlite.foreign.sqlite3_aggregate_context as native_sqlite3_aggregate_context
import ksqlite.foreign.sqlite3_column_blob as native_sqlite3_column_blob
import ksqlite.foreign.sqlite3_get_auxdata as native_sqlite3_get_auxdata
import ksqlite.foreign.sqlite3_serialize as native_sqlite3_serialize
import ksqlite.foreign.sqlite3_set_auxdata as native_sqlite3_set_auxdata
import ksqlite.foreign.sqlite3_user_data as native_sqlite3_user_data
import ksqlite.foreign.sqlite3_value_blob as native_sqlite3_value_blob
import ksqlite.foreign.sqlite3_value_pointer as native_sqlite3_value_pointer

private val pointerSize = sizeOf<CPointerVarOf<*>>().toInt()

internal actual fun columnBufferInternal(
    stmt: sqlite3_stmt,
    index: Int
): Buffer? = commonColumnBuffer(
    stmt = stmt,
    index = index,
    pointer = native_sqlite3_column_blob(stmt.pointer, index),
    toBuffer = Buffer::from
)

internal actual fun valueBufferInternal(value: sqlite3_value): Buffer? = commonValueBuffer(
    value = value,
    pointer = native_sqlite3_value_blob(value.pointer),
    toBuffer = Buffer::from
)

internal actual fun aggregateContextInternal(
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

internal actual fun getAuxdataInternal(context: sqlite3_context, index: Int): Long? {
    return native_sqlite3_get_auxdata(context.pointer, index)?.toLong()
}

internal actual fun setAuxdataInternal(
    context: sqlite3_context,
    index: Int,
    destroy: SqliteDestroyCallback<Nothing?>
): Long? = context.db.withMemoryManager {
    val pointer = stableRefPointer(null, null, destroy)
    val disposer = stableRefDisposer(null, destroy)
    native_sqlite3_set_auxdata(context.pointer, index, pointer, disposer)
    pointer?.toLong()
}

@PublishedApi
internal actual fun userDataInternal(context: sqlite3_context): ApplicationDefinedFunction<*>? {
    val pointer = native_sqlite3_user_data(context.pointer) ?: return null
    return stableRefData<ApplicationDefinedFunction<*>>(pointer)
}

public actual fun serializeInternal(
    db: sqlite3,
    database: String?,
    outSize: Int64OutputParam,
    flags: SqliteSerializeFlag?
): Buffer? = Buffer.from(
    pointer = memScoped {
        useParam(outSize) { sizePtr ->
            val mFlags = flags?.value?.convert() ?: 0u
            native_sqlite3_serialize(db.pointer, database?.cstr?.ptr, sizePtr, mFlags)
        }
    },
    size = outSize.value
)


@PublishedApi
internal actual fun valuePointerInternal(
    value: sqlite3_value,
    type: String?
): Any? {
    val pointer = native_sqlite3_value_pointer(value.pointer, type) ?: return null
    return stableRefAppData(pointer)
}