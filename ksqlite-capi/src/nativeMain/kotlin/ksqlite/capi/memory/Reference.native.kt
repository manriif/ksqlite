package ksqlite.capi.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Reference to an object preventing GC from collecting or moving it.
 */
internal interface Reference : Disposable {

    /**
     * The ser data if any.
     */
    val userData: sqlite3_mutable_pointer?

    /**
     * Returns the object instance.
     */
    fun <Data : Any> get(): Data
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * C-static function disposing a [Reference].
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val RefDisposer = staticCFunction { pointer: COpaquePointer? ->
    checkNotNull(pointer)
    pointer.asStableRef<Reference>().get().dispose()
}

/**
 * Returns [RefDisposer] only if [data] != `null` or [destructor] != `null`.
 */
internal fun refDisposer(
    data: Any?,
    destructor: Sqlite3DestructorCallback? = null
): CPointer<CFunction<(COpaquePointer?) -> Unit>>? {
    return RefDisposer.takeIf { data != null || destructor != null }
}

/**
 * Returns the object [Data] backed by `this` [COpaquePointer] with an optional user data pointer.
 * Throws [IllegalStateException] if `this` [COpaquePointer] is `null`.
 */
internal fun <Data : Any> COpaquePointer?.data(): Pair<Data, sqlite3_mutable_pointer?> {
    checkNotNull(this)

    return asStableRef<Reference>().get().run {
        get<Data>() to userData
    }
}