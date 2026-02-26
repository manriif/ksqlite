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
 *
 * Throws [IllegalStateException] if the [COpaquePointer] passed to the function is `null`.
 */
private val ReferenceDisposer = staticCFunction { pointer: COpaquePointer? ->
    val _ = disposeRef(pointer)
}

/**
 * Returns [ReferenceDisposer] only if [data] != `null` or [destructor] != `null`.
 */
internal fun refDisposer(
    data: Any?,
    destructor: Sqlite3DestructorCallback? = null
): CPointer<CFunction<(COpaquePointer?) -> Unit>>? {
    return ReferenceDisposer.takeIf { data != null || destructor != null }
}

/**
 * Returns the object [Data] backed by `this` [COpaquePointer] with an optional user data pointer.
 *
 * Throws [IllegalStateException] if `this` [COpaquePointer] is `null`.
 */
internal fun <Data : Any> refData(pointer: COpaquePointer?): Pair<Data, sqlite3_mutable_pointer?> {
    checkNotNull(pointer)

    return pointer.asStableRef<Reference>().get().run {
        get<Data>() to userData
    }
}

/**
 * Disposes the object referenced [pointer] and returns the associated user data if any.
 *
 * Throws [IllegalStateException] if `this` [COpaquePointer] is `null`.
 */
@IgnorableReturnValue
internal fun disposeRef(pointer: COpaquePointer?): sqlite3_mutable_pointer? {
    checkNotNull(pointer)

    return pointer.asStableRef<Reference>().get().run {
        userData?.also {
            dispose()
        }
    }
}