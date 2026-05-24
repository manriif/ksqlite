package ksqlite.capi.memory

import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmPointer

/**
 * Returns the object [Data] backed by [pointer] with an optional user data pointer.
 *
 * Throws [IllegalStateException] if [pointer] is [NullPtr].
 */
internal inline fun <reified Data : Any, AppData> MemoryManager.stableRefData(
    pointer: WasmPointer
): ReferencedData<Data, AppData> {
    check(!pointer.isNull) { "Pointer must not point to null" }
    return getStableRef<AppData>(pointer).getReferencedData()
}