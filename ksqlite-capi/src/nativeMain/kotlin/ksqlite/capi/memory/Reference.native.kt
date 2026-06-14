package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.SqliteDestroyCallback

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
    destructor: SqliteDestroyCallback<*>? = null
) = StableRefDisposer.takeIf { data != null || destructor != null }

/**
 * Returns the [DataHolder] referenced by [pointer].
 */
internal inline fun <reified Data : Any, AppData> stableRefDataHolder(
    pointer: COpaquePointer?
): DataHolder<Data, AppData> = checkNotNull(pointer) { "Pointer must not be null" }
    .asStableRef<Reference<AppData>>()
    .get()
    .cast()

/**
 * Returns the [Data] referenced by [pointer].
 */
internal inline fun <reified Data : Any> stableRefData(pointer: COpaquePointer?): Data =
    stableRefDataHolder<Data, Any?>(pointer).data

/**
 * Returns the [AppData] referenced by [pointer].
 */
internal fun <AppData> stableRefAppData(pointer: COpaquePointer?): AppData =
    stableRefDataHolder<Any, AppData>(pointer).appData