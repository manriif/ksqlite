package ksqlite

import java.lang.foreign.MemorySegment

/**
 * Returns a [pointer] wrapping `this` [MemorySegment].
 */
internal fun wrap(segment: MemorySegment): pointer? = segment?.let(::pointer)