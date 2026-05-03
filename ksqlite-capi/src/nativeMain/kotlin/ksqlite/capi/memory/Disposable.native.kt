package ksqlite.capi.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Destructor signature.
 */
internal typealias Disposer = CPointer<CFunction<(COpaquePointer?) -> Unit>>

///////////////////////////////////////////////////////////////////////////
// Global
///////////////////////////////////////////////////////////////////////////

/**
 * Holds any [Disposable] that should be reachable by static C function given a pointer.
 * TODO must be thread-safe
 */
private val GlobalDisposables: MutableMap<COpaquePointer, Disposable> by lazy(::hashMapOf)

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
 * [Disposable] invoking [destructor] with [userData] when disposed.
 */
private class UserDataDisposable(
    private val userData: sqlite3_mutable_pointer,
    private val destructor: Sqlite3DestructorCallback
) : Disposable {

    override fun dispose() {
        unregisterGlobalDisposable(userData.block.pointer)
        destructor(userData)
    }
}

/**
 * Registers a [Disposable] which will invoke [destructor] when disposed.
 * Returns [GlobalDisposer] only if [userData] != `null` and [destructor] != `null`.
 */
internal fun userDataDisposer(
    userData: sqlite3_mutable_pointer?,
    destructor: Sqlite3DestructorCallback?
): Disposer? {
    if (userData == null || destructor == null) {
        return null
    }

    registerGlobalDisposable(
        pointer = userData.block.pointer,
        disposable = UserDataDisposable(
            userData = userData,
            destructor = destructor
        )
    )

    return GlobalDisposer
}