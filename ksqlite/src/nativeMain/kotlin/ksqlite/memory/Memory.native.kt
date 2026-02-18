package ksqlite.memory

import kotlinx.cinterop.COpaquePointer
import ksqlite.types.pointer

/**
 * Returns a [pointer] wrapping `this` [COpaquePointer].
 */
internal fun wrap(pointer: COpaquePointer?): pointer? = pointer?.let(::pointer)