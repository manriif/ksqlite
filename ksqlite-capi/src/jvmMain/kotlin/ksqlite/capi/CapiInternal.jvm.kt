@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.allocateUtf8
import ksqlite.capi.memory.globalMemory
import ksqlite.capi.memory.memScoped
import ksqlite.capi.memory.memory
import ksqlite.capi.memory.orNull
import ksqlite.capi.memory.stableRefAppData
import ksqlite.capi.memory.stableRefData
import ksqlite.capi.memory.stableRefDisposer
import ksqlite.capi.memory.useParam
import ksqlite.capi.memory.withMemoryManager
import ksqlite.types.SqliteSerializeFlag
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout
import ksqlite.foreign.sqlite3 as native

private val pointerSize = ValueLayout.ADDRESS.byteSize().toInt()

internal actual fun columnBufferInternal(
    stmt: sqlite3_stmt,
    index: Int
): Buffer? = commonColumnBuffer(
    stmt = stmt,
    index = index,
    pointer = native.sqlite3_column_blob(stmt.pointer, index),
    toBuffer = Buffer::from
)

internal actual fun valueBufferInternal(value: sqlite3_value): Buffer? = commonValueBuffer(
    value = value,
    pointer = native.sqlite3_value_blob(value.pointer),
    toBuffer = Buffer::from
)

internal actual fun aggregateContextInternal(
    context: sqlite3_context,
    create: Boolean
): Long? {
    val pointer = if (create) {
        native.sqlite3_aggregate_context(context.pointer, pointerSize)
    } else {
        native.sqlite3_aggregate_context(context.pointer, 0)
    }

    return pointer.orNull?.address()
}

internal actual fun getAuxdataInternal(context: sqlite3_context, index: Int): Long? {
    return native.sqlite3_get_auxdata(context.pointer, index).orNull?.address()
}

internal actual fun setAuxdataInternal(
    context: sqlite3_context,
    index: Int,
    destroy: SqliteDestroyCallback<Nothing?>
): Long? = context.db.withMemoryManager {
    val pointer = stableRefPointer(null, null, destroy)
    val disposer = stableRefDisposer(null, destroy)
    native.sqlite3_set_auxdata(context.pointer, index, pointer, disposer)
    pointer.orNull?.address()
}

@PublishedApi
internal actual fun userDataInternal(context: sqlite3_context): ApplicationDefinedFunction<*>? {
    val pointer = native.sqlite3_user_data(context.pointer).orNull ?: return null
    return context.db.memory.stableRefData<ApplicationDefinedFunction<*>>(pointer)
}

public actual fun serializeInternal(
    db: sqlite3,
    database: String?,
    outSize: Int64OutputParam,
    flags: SqliteSerializeFlag?
): Buffer? = Buffer.from(
    pointer = memScoped {
        useParam(outSize) { sizePtr ->
            val mFlags = flags?.value ?: 0
            native.sqlite3_serialize(db.pointer, database.allocateUtf8(), sizePtr, mFlags)
        }
    },
    size = outSize.value
)

@PublishedApi
internal actual fun valuePointerInternal(
    value: sqlite3_value,
    type: String?
): Any? = memScoped {
    val pointer = native.sqlite3_value_pointer(value.pointer, type.allocateUtf8()).orNull
        ?: return null

    globalMemory.stableRefAppData(pointer)
}

///////////////////////////////////////////////////////////////////////////
// Utils
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a pointer to a string holding [text]'s content, obtained through
 * [native.sqlite3_mprintf].
 */
context(allocator: SegmentAllocator)
internal fun sqlite3_mprintf(text: String): MemorySegment = native.sqlite3_mprintf
    .makeInvoker()
    .apply(text.allocateUtf8(allocator))

/**
 * Returns a pointer to a string holding [text]'s content, obtained through
 * [native.sqlite3_mprintf]. Returns [NullPtr] if [text] is `null`.
 */
internal fun sqlite3_mprintf(text: String?): MemorySegment {
    if (text == null) {
        return NullPtr
    }

    return Arena.ofConfined().use { arena ->
        with(arena) {
            sqlite3_mprintf(text)
        }
    }
}