package ksqlite.capi.memory

import ksqlite.capi.handlers.Handler
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction

/**
 * Handler that dispose reference to object.
 */
internal class StableRefDisposerHandler : Handler() {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = { refPointer: WasmPointer ->
            manager.getStableRef<Nothing?>(refPointer).dispose()
        }
    )
}

/**
 * Returns the object [Data] backed by [pointer] with an optional user data pointer.
 *
 * Throws [IllegalStateException] if [pointer] is [NullPtr].
 */
internal inline fun <reified Data : Any, AppData> MemoryManager.stableRefDataHolder(
    pointer: WasmPointer
): DataHolder<Data, AppData> {
    check(!pointer.isNull) { "Pointer must not point to null" }
    return getStableRef<AppData>(pointer).cast()
}

/**
 * Returns the [Data] referenced by [pointer].
 */
internal inline fun <reified Data : Any> MemoryManager.stableRefData(pointer: WasmPointer): Data =
    stableRefDataHolder<Data, Any?>(pointer).data

/**
 * Returns the [AppData] referenced by [pointer].
 */
internal fun <AppData> MemoryManager.stableRefAppData(pointer: WasmPointer): AppData =
    stableRefDataHolder<Any, AppData>(pointer).appData