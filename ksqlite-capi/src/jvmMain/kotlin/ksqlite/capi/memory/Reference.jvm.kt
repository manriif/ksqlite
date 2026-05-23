package ksqlite.capi.memory

import java.lang.foreign.MemorySegment

/**
 * Returns the object [Data] backed by [pointer] with an optional app data pointer.
 *
 * Throws [IllegalStateException] if [pointer] is [MemorySegment.NULL].
 */
internal inline fun <reified Data : Any, AppData> MemoryManager.stableRefData(
    pointer: MemorySegment
): ReferencedData<Data, AppData> {
    check(!pointer.isNull) { "Pointer must not point to null" }
    return getStableRef<AppData>(pointer).getReferencedData()
}