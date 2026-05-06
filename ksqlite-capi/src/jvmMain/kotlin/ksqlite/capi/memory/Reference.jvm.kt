package ksqlite.capi.memory

import java.lang.foreign.MemorySegment


/**
 * Returns the object [Data] backed by [pointer] with an optional user data pointer.
 *
 * Throws [IllegalStateException] if [pointer] is [MemorySegment.NULL].
 */
internal inline fun <reified Data : Any> MemoryManager.stableRefData(
    pointer: MemorySegment
): ReferencedData<Data> {
    check(!pointer.isNull) { "Pointer must not be null" }
    return getStableRef(pointer).getReferencedData()
}