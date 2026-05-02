package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer

public actual open class GenericPointer internal constructor(
    internal open val pointer: COpaquePointer
)

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

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
    userData: sqlite3_mutable_pointer? = null,
    destructor: Sqlite3DestructorCallback? = null,
): COpaquePointer? = stableRefPointer(
    data = data,
    userData = userData,
    destructor = destructor,
    key = key
)