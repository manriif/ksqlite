package ksqlite.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction

/**
 * Holds reference to an object preventing GC operations on it.
 */
internal interface ManagedPointer {

    /**
     * Returns the object instance.
     */
    fun <Data : Any> get(): Data

    /**
     * Releases the object and making it available to GC.
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
 * Returns [ManagedDestructor] only if `this` != `null`.
 */
internal fun Any?.managedDestructor(): CPointer<CFunction<(COpaquePointer?) -> Unit>>? {
    return ManagedDestructor.takeIf { this != null }
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
    asStableRef<ManagedPointer>().dispose()
}