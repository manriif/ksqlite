package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import ksqlite.capi.types.sqlite3_mutable_pointer

public actual open class GenericPointer internal constructor(
    internal open val pointer: COpaquePointer
)

/**
 * Returns a stable [COpaquePointer] to [value] available globally.
 * Returns `null` if [value] is `null`.
 *
 * [value] can later be accessed within a callback using [userData] and disposed using
 * [refDisposer].
 *
 * If a pointer was previously obtained using [key], it is disposed.
 */
internal fun MemoryManager.keyedRefPointer(
    key: String,
    value: Any?,
    userData: sqlite3_mutable_pointer? = null
): COpaquePointer? = refPointer(
    data = value,
    userData = userData,
    key = key
)