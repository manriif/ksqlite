package ksqlite.capi.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.Sqlite3DestroyCallback

/**
 * Destructor signature.
 */
internal typealias Disposer = CPointer<CFunction<(COpaquePointer?) -> Unit>>

///////////////////////////////////////////////////////////////////////////
// Global
///////////////////////////////////////////////////////////////////////////

/**
 * Holds any [Disposable] that should be reachable by static C function given a pointer.
 */
private val GlobalDisposables by lazy { ConcurrentMap<COpaquePointer, Disposable>() }

/**
 * C-static function disposing a [Disposable] registered with [registerGlobalDisposable].
 *
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val GlobalDisposer = staticCFunction { pointer: COpaquePointer? ->
    checkNotNull(pointer)
    checkNotNull(GlobalDisposables[pointer]).dispose()

    // It is the owner responsibility to unregister the disposable after dispose have been called
    check(GlobalDisposables[pointer] == null)
}

/**
 * Returns [GlobalDisposer] only if [data] != `null`.
 */
internal fun globalDisposer(data: Any?): Disposer? {
    return GlobalDisposer.takeIf { data != null }
}

/**
 * Registers [disposable] associated with [pointer].
 *
 * The owner of the [Disposable] must call [unregisterGlobalDisposable] when [Disposable.dispose]
 * is invoked.
 *
 * The registered [disposable] can later be disposed using [globalDisposer].
 */
internal fun registerGlobalDisposable(pointer: COpaquePointer, disposable: Disposable) {
    check(GlobalDisposables.put(pointer, disposable) == null) {
        "A disposable is already registered for the pointed address"
    }
}

/**
 * Unregisters a previously registered [Disposable] associated with [pointer].
 */
internal fun unregisterGlobalDisposable(pointer: COpaquePointer) {
    check(GlobalDisposables.remove(pointer) != null) {
        "No disposable was registered fo the pointed address"
    }
}

///////////////////////////////////////////////////////////////////////////
// User data
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
        destructor.handle(buffer)
    }
}

/**
 * Registers a [Disposable] which will invoke [destructor] when disposed.
 * If [destructor] is `null` then `null` is returned.
 */
internal fun bufferDisposer(
    buffer: Buffer,
    destructor: Sqlite3DestroyCallback<Buffer>?
): Disposer? {
    if (destructor == null) {
        return null
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