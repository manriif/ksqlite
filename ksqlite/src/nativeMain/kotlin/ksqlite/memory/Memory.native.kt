package ksqlite.memory

import kotlinx.cinterop.CFunction
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.asStableRef
import kotlinx.cinterop.staticCFunction
import ksqlite.types.Sqlite3DestructorCallback

///////////////////////////////////////////////////////////////////////////
// Reference
///////////////////////////////////////////////////////////////////////////

/**
 * Destructor releasing a [Reference].
 */
private val ReferenceDestructor = staticCFunction { pointer: COpaquePointer? ->
    pointer.releaseReference()
}

/**
 * Returns [ReferenceDestructor] only if [data] != `null` or [destructor] != `null`.
 */
internal fun referenceDestructor(
    data: Any?,
    destructor: Sqlite3DestructorCallback? = null
): CPointer<CFunction<(COpaquePointer?) -> Unit>>? {
    return ReferenceDestructor.takeIf { data != null || destructor != null }
}

/**
 * Returns the object [Data] backed by `this` [COpaquePointer].
 * Throws [IllegalStateException] if `this` [COpaquePointer] is `null`.
 */
internal fun <Data : Any> COpaquePointer?.getReferencedData(): Data {
    checkNotNull(this)
    return asStableRef<Reference>().get().get()
}

/**
 * Releases the object referenced by `this` [COpaquePointer]
 * Throws [IllegalStateException] if `this` [COpaquePointer] is `null`.
 */
internal fun COpaquePointer?.releaseReference() {
    checkNotNull(this)
    asStableRef<Reference>().get().release()
}