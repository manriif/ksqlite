package ksqlite.capi.memory

import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator

public actual open class GenericPointer internal constructor(internal val pointer: MemorySegment)

/**
 * Memory manager that is never cleared.
 */
internal val StaticMemoryManager = MemoryManager()

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a stable [MemorySegment] to [data] available globally.
 * Returns `null` if [data] is `null`.
 *
 * The resulting reference data can be accessed using [MemoryManager.getStableRef] and it can be
 * disposed using [MemoryManager.stableRefDisposer].
 *
 * If a pointer was previously obtained using [key], it is disposed.
 */
internal fun MemoryManager.keyedStableRefPointer(
    key: String,
    data: Any?,
    userData: sqlite3_mutable_pointer? = null,
    destructor: Sqlite3DestructorCallback? = null,
): MemorySegment = stableRefPointer(
    data = data,
    userData = userData,
    destructor = destructor,
    key = key
)

///////////////////////////////////////////////////////////////////////////
// Arena
///////////////////////////////////////////////////////////////////////////

/**
 * Runs given [block] providing allocation of memory which will be automatically disposed at the end
 * of this scope.
 */
internal inline fun <T> memScoped(block: SegmentAllocator.() -> T): T {
    return Arena.ofConfined().use(block)
}