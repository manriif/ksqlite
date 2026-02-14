package ksqlite

import java.lang.foreign.MemorySegment

///////////////////////////////////////////////////////////////////////////
// Segment
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the [MemorySegment] obtained from [block] provided non-null [value] or returns
 * [MemorySegment.NULL] if [value] is `null`.
 */
internal inline fun <T : Any> segment(value: T?, block: (T) -> MemorySegment): MemorySegment {
    return if (value == null) MemorySegment.NULL else block(value)
}

/**
 * Returns a [MemorySegment] to `this` [ByteArray]'s content.
 *
 * /!\ JVM GC moves memory so the content must be copied on native side and at call site. If these
 * constraints are not satisfied then invalid memory region will be accessed.
 */
internal fun ByteArray?.pointer(): MemorySegment {
    return segment(this, MemorySegment::ofArray)
}

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a [functionPointer] wrapping `this` [MemorySegment].
 */
internal fun wrap(segment: MemorySegment): pointer? = segment
    .takeUnless { it.address() == MemorySegment.NULL.address() }
    ?.let(::pointer)