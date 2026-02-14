package ksqlite

import kotlinx.cinterop.COpaquePointer

/**
 * Returns a [pointer] wrapping `this` [COpaquePointer].
 */
internal fun wrap(pointer: COpaquePointer?): pointer? = pointer?.let(::pointer)