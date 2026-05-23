package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.Sqlite3DestroyCallback

/**
 * C-static function disposing a [Reference] from a [kotlinx.cinterop.StableRef].
 *
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val StableRefDisposer = staticCFunction { pointer: COpaquePointer? ->
    val _ = disposeStableRef(pointer)
}

/**
 * Returns [StableRefDisposer] only if [data] != `null` or [destructor] != `null`.
 */
internal fun stableRefDisposer(
    data: Any?,
    destructor: Sqlite3DestroyCallback? = null
): Disposer? {
    return StableRefDisposer.takeIf { data != null || destructor != null }
}

/**
 * Returns a stable [COpaquePointer] to [data] available globally.
 * Returns `null` if [data] is `null`.
 *
 * [data] can later be accessed within a callback using [stableRefData] and disposed using
 * [stableRefDisposer].
 *
 * If a pointer was previously obtained using [key], it is disposed.
 */
internal fun MemoryManager.keyedStableRefPointer(
    key: String,
    data: Any?,
    userData: Buffer? = null,
    destructor: Sqlite3DestroyCallback? = null,
): COpaquePointer? = stableRefPointer(
    data = data,
    userData = userData,
    destructor = destructor,
    key = key
)

/**
 * Returns the object [Data] backed by [pointer] with an optional user data pointer.
 *
 * Throws [IllegalStateException] if [pointer] is `null`.
 */
internal inline fun <reified Data : Any> stableRefData(pointer: COpaquePointer?): ReferencedData<Data> {
    return checkNotNull(pointer) { "Pointer must not be null" }
        .asStableRef<Reference>()
        .get()
        .getReferencedData()
}

/**
 * Disposes the object referenced by [pointer] and returns the associated user data pointer if any.
 *
 * Throws [IllegalStateException] if `this` [COpaquePointer] is `null`.
 */
@IgnorableReturnValue
internal fun disposeStableRef(pointer: COpaquePointer?): Buffer? {
    checkNotNull(pointer)

    return pointer.asStableRef<Reference>().get().run {
        userData?.also {
            dispose()
        }
    }
}