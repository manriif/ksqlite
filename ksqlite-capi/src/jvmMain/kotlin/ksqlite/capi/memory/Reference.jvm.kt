package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestructorCallback
import java.lang.foreign.MemorySegment

/**
 * Replaces an existing reference and returns previous [ClientData].
 * The old reference pointer must be returned by
 *
 * It is passed a [MemorySegment] to [block] corresponding to what returned by a call to
 * [keyedStableRefPointer] with supplied arguments.
 */
internal inline fun <reified ClientData> MemoryManager.replaceKeyedStableRefData(
    key: String,
    data: Any?,
    newData: ClientData,
    destructor: Sqlite3DestructorCallback<ClientData>? = null,
    block: MemoryManager.(newRefPointer: MemorySegment) -> MemorySegment
): ClientData? {
    val newRefPointer = keyedStableRefPointer(key, data, newData, destructor) {

    }
}

/**
 * Returns the object [Data] backed by [pointer] with an optional user data pointer.
 *
 * Throws [IllegalStateException] if [pointer] is [MemorySegment.NULL].
 */
internal inline fun <reified Data : Any, ClientData> MemoryManager.stableRefData(
    pointer: MemorySegment
): ReferencedData<Data, ClientData> {
    check(!pointer.isNull) { "Pointer must not point to null" }
    return getStableRef<ClientData>(pointer).getReferencedData()
}