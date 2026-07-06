package ksqlite.capi.memory

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.handlers.Handler
import ksqlite.foreign.wasm.WasmFunctions
import ksqlite.foreign.wasm.WasmPointer

/**
 * Handler that dispose reference to object.
 */
internal class StableRefDisposerHandler : Handler(), ReferenceFunction {

    override fun install(functions: WasmFunctions): WasmPointer =
        functions.installReferenceFunction(this)

    override fun apply(refPointer: WasmPointer) {
        manager.stableRefDisposable(refPointer).dispose()
    }
}

/**
 * Returns the [StableRefDisposerHandler] instance of `this` [MemoryManager] only if
 * [data] != `null` or [destructor] != `null`. [NullPtr] is returned otherwise.
 */
internal fun MemoryManager.stableRefDisposer(
    data: Any?,
    destructor: SqliteDestroyCallback<*>? = null
): WasmPointer = stableRefDisposer.takeIf { data != null || destructor != null } ?: NullPtr

/**
 * Returns the [Disposable] referenced by [pointer].
 */
internal fun MemoryManager.stableRefDisposable(pointer: WasmPointer): Disposable =
    getStableRef<Nothing?>(pointer)

/**
 * Returns the object [Data] referenced by [pointer] with an optional user data pointer.
 *
 * Throws [IllegalStateException] if [pointer] is [NullPtr].
 */
internal inline fun <reified Data : Any, AppData> MemoryManager.stableRefDataHolder(
    pointer: WasmPointer
): DataHolder<Data, AppData> = getStableRef<AppData>(pointer).cast()

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