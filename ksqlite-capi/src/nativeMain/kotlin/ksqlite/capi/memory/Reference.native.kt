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
    checkNotNull(pointer)
    pointer.asStableRef<Reference<*>>().get().dispose()
}

/**
 * Returns [StableRefDisposer] only if [data] != `null` or [destructor] != `null`.
 */
internal fun stableRefDisposer(
    data: Any?,
    destructor: Sqlite3DestroyCallback<*>? = null
): Disposer? {
    return StableRefDisposer.takeIf { data != null || destructor != null }
}

/**
 * Returns the object [Data] backed by [pointer] with an optional user data pointer.
 *
 * Throws [IllegalStateException] if [pointer] is `null`.
 */
internal inline fun <reified Data : Any, AppData> stableRefData(
    pointer: COpaquePointer?
): ReferencedData<Data, AppData> {
    return checkNotNull(pointer) { "Pointer must not be null" }
        .asStableRef<Reference<AppData>>()
        .get()
        .getReferencedData()
}