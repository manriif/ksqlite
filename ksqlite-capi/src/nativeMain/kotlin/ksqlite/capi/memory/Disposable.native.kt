package ksqlite.capi.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction

/**
 * Disposables living shortly.
 */
private val Disposables: MutableMap<COpaquePointer, Disposable> by lazy(::hashMapOf)

/**
 * C-static function disposing a [Disposable].
 *
 * The owner of the [Disposable] must call [unregisterGlobalDisposable] when [Disposable.dispose]
 * is invoked.
 *
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val GlobalDisposer = staticCFunction { pointer: COpaquePointer? ->
    checkNotNull(pointer)
    checkNotNull(Disposables[pointer]).dispose()
    check(Disposables[pointer] == null)
}

/**
 * Returns [GlobalDisposer] only if [data] != `null`.
 */
internal fun globalDisposer(data: Any?): CPointer<CFunction<(COpaquePointer?) -> Unit>>? {
    return GlobalDisposer.takeIf { data != null }
}

/**
 * Registers [disposable] pointed by [pointer].
 * [disposable] is expected to be disposed later and a [globalDisposer] should be requested to
 * dispose it.
 */
internal fun registerGlobalDisposable(pointer: COpaquePointer, disposable: Disposable) {
    check(Disposables.put(pointer, disposable) == null) {
        "A disposable is already registered for the pointed address"
    }
}

/**
 * Unregisters a previously registered [Disposable] pointed by [pointer].
 */
internal fun unregisterGlobalDisposable(pointer: COpaquePointer) {
    check(Disposables.remove(pointer) != null) {
        "No disposable was registered fo the pointed address"
    }
}