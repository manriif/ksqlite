package ksqlite.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import ksqlite.types.Sqlite3DestructorCallback

/**
 * Holds reference to an object preventing GC operations on it.
 */
internal interface ManagedPointer {

    /**
     * Returns the object instance.
     */
    fun <Data : Any> get(): Data

    /**
     * Releases the object making it available to GC.
     * Also release the associated StableRef if any.
     */
    fun dispose()
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Destructor disposing a [ManagedPointer].
 */
internal val ManagedDestructor = staticCFunction { pointer: COpaquePointer? ->
    pointer.disposeManaged()
}

/**
 * Returns [ManagedDestructor] only if [target] != `null` or [destructor] != `null`.
 */
internal fun managedDestructor(
    target: Any?,
    destructor: Sqlite3DestructorCallback? = null
): CPointer<CFunction<(COpaquePointer?) -> Unit>>? {
    return ManagedDestructor.takeIf { target != null || destructor != null }
}

/**
 * Returns the object [Data] backed by `this` [COpaquePointer].
 * Throws [IllegalStateException] if `this` [COpaquePointer] is `null`.
 */
internal fun <Data : Any> COpaquePointer?.getManaged(): Data {
    checkNotNull(this)
    return asStableRef<ManagedPointer>().get().get()
}

/**
 * Disposes the object referenced by `this` [COpaquePointer]
 * Throws [IllegalStateException] if `this` [COpaquePointer] is `null`.
 */
internal fun COpaquePointer?.disposeManaged() {
    checkNotNull(this)
    asStableRef<ManagedPointer>().get().dispose()
}