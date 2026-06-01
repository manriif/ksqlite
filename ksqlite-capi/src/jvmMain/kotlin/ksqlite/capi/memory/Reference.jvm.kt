package ksqlite.capi.memory

import ksqlite.capi.handlers.ReferenceHandler
import java.lang.foreign.MemorySegment

/**
 * Handler that dispose reference to object.
 */
internal class StableRefDisposerHandler : ReferenceHandler() {

    override fun apply(refPointer: MemorySegment) {
        manager.getStableRef<Nothing?>(refPointer).dispose()
    }
}

/**
 * Returns the object [Data] backed by [pointer] with an optional app data pointer.
 */
internal inline fun <reified Data : Any, AppData> MemoryManager.stableRefDataHolder(
    pointer: MemorySegment
): DataHolder<Data, AppData> {
    check(!pointer.isNull) { "Pointer must not point to null" }
    return getStableRef<AppData>(pointer).cast()
}

/**
 * Returns the [Data] referenced by [pointer].
 */
internal inline fun <reified Data : Any> MemoryManager.stableRefData(pointer: MemorySegment): Data =
    stableRefDataHolder<Data, Any?>(pointer).data

/**
 * Returns the [AppData] referenced by [pointer].
 */
internal fun <AppData> MemoryManager.stableRefAppData(pointer: MemorySegment): AppData =
    stableRefDataHolder<Any, AppData>(pointer).appData