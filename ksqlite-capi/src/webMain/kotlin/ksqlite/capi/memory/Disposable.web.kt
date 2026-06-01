package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.handlers.Handler
import ksqlite.capi.interop.wasm.FunctionSignature
import ksqlite.capi.interop.wasm.NullPtr
import ksqlite.capi.interop.wasm.WasmFunctions
import ksqlite.capi.interop.wasm.WasmPointer
import ksqlite.capi.interop.wasm.installFunction

///////////////////////////////////////////////////////////////////////////
// Global
///////////////////////////////////////////////////////////////////////////

/**
 * Holds any [Disposable] that should be reachable by static C function given a pointer.
 */
private val GlobalDisposables by lazy { mutableMapOf<WasmPointer, Disposable>() }

/**
 * Pointer to a static function disposing a [Disposable] registered with [registerGlobalDisposable].
 */
private val GlobalDisposer: WasmPointer = StaticMemoryManager.functionPointer(::DisposerHandler)

/**
 * Handler that dispose reference to object to make it available for GC.
 */
private class DisposerHandler(manager: MemoryManager) : Handler(manager) {

    override fun install(functions: WasmFunctions): WasmPointer = functions.installFunction(
        signature = FunctionSignature.Void(FunctionSignature.Pointer),
        function = v@{ dataPointer: WasmPointer ->
            checkNotNull(GlobalDisposables[dataPointer]).dispose()
            // It is the owner responsibility to unregister the disposable after dispose has been
            // called
            check(GlobalDisposables[dataPointer] == null)
        }
    )
}

/**
 * Returns [GlobalDisposer] or [NullPtr] if [data] is `null`.
 */
internal fun globalDisposer(data: Any?): WasmPointer {
    return GlobalDisposer.takeIf { data != null } ?: NullPtr
}

/**
 * Registers [disposable] associated with [pointer].
 *
 * The owner of the [Disposable] must call [unregisterGlobalDisposable] when [Disposable.dispose]
 * is invoked.
 *
 * The registered [disposable] can later be disposed using [globalDisposer].
 */
internal fun registerGlobalDisposable(pointer: WasmPointer, disposable: Disposable) {
    check(GlobalDisposables.put(pointer, disposable) == null) {
        "A disposable is already registered for the pointed address"
    }
}

/**
 * Unregisters a previously registered [Disposable] associated with [pointer].
 */
internal fun unregisterGlobalDisposable(pointer: WasmPointer) {
    check(GlobalDisposables.remove(pointer) != null) {
        "No disposable was registered fo the pointed address"
    }
}

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * [Disposable] invoking [destructor] with [buffer] when disposed.
 */
private class BufferDisposer(
    private val buffer: Buffer,
    private val destructor: Sqlite3DestroyCallback<Buffer>
) : Disposable {

    override fun dispose() {
        unregisterGlobalDisposable(buffer.pointer)
        destructor.apply(buffer)
    }
}

/**
 * Registers a [Disposable] which will invoke [destructor] when disposed.
 * If [destructor] is `null` then [NullPtr] is returned.
 */
internal fun bufferDisposer(
    buffer: Buffer,
    destructor: Sqlite3DestroyCallback<Buffer>?
): WasmPointer {
    if (destructor == null) {
        return NullPtr
    }

    registerGlobalDisposable(
        pointer = buffer.pointer,
        disposable = BufferDisposer(
            buffer = buffer,
            destructor = destructor
        )
    )

    return GlobalDisposer
}