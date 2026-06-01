package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.handlers.ReferenceHandler
import java.lang.foreign.MemorySegment
import java.util.concurrent.ConcurrentHashMap

///////////////////////////////////////////////////////////////////////////
// Global
///////////////////////////////////////////////////////////////////////////

/**
 * Holds any [Disposable] that should be reachable by static C function given a pointer.
 */
private val GlobalDisposables by lazy { ConcurrentHashMap<Long, Disposable>() }

/**
 * Pointer to a static function disposing a [Disposable] registered with [registerGlobalDisposable].
 */
private val GlobalDisposer: MemorySegment = StaticMemoryManager.functionPointer(::DisposerHandler)

/**
 * Handler that dispose reference to object to make it available for GC.
 */
private class DisposerHandler : ReferenceHandler() {

    override fun apply(refPointer: MemorySegment) {
        val address = refPointer.address()
        checkNotNull(GlobalDisposables[address]).dispose()
        // It is the owner responsibility to unregister the disposable after dispose has been called
        check(GlobalDisposables[address] == null)
    }
}

/**
 * Returns [GlobalDisposer] or [MemorySegment.NULL] if [data] is `null`.
 */
internal fun globalDisposer(data: Any?): MemorySegment {
    return GlobalDisposer.takeIf { data != null } ?: MemorySegment.NULL
}

/**
 * Registers [disposable] associated with [pointer].
 *
 * The owner of the [Disposable] must call [unregisterGlobalDisposable] when [Disposable.dispose]
 * is invoked.
 *
 * The registered [disposable] can later be disposed using [globalDisposer].
 */
internal fun registerGlobalDisposable(pointer: MemorySegment, disposable: Disposable) {
    check(GlobalDisposables.put(pointer.address(), disposable) == null) {
        "A disposable is already registered for the pointed address"
    }
}

/**
 * Unregisters a previously registered [Disposable] associated with [pointer].
 */
internal fun unregisterGlobalDisposable(pointer: MemorySegment) {
    check(GlobalDisposables.remove(pointer.address()) != null) {
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
 * If [destructor] is `null` then [MemorySegment.NULL] is returned.
 */
internal fun bufferDisposer(
    buffer: Buffer,
    destructor: Sqlite3DestroyCallback<Buffer>?
): MemorySegment {
    if (destructor == null) {
        return MemorySegment.NULL
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