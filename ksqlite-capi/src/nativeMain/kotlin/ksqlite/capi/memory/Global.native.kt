package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.sqlite3_mutable_pointer

internal actual class RawPointer(val pointer: COpaquePointer)

internal actual fun disposeRawPointer(pointer: RawPointer) {
    disposeRef(pointer.pointer)
}

/**
 * Returns a global [COpaquePointer] resulting from a call to [MemoryManager.refPointer] with
 * supplied arguments.
 */
internal fun globalRefPointer(
    key: String,
    value: Any?,
    userData: sqlite3_mutable_pointer? = null,
    destructor: Sqlite3DestructorCallback? = null
): COpaquePointer? {
    if (value == null) {
        return null
    }

    return globalPointer(key) {
        refPointer(value, userData, destructor)?.let(::RawPointer)
    }?.pointer
}