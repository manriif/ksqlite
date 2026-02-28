package ksqlite.capi.memory

import java.lang.foreign.MemorySegment

public actual open class GenericPointer internal constructor(internal val pointer: MemorySegment)

///////////////////////////////////////////////////////////////////////////
// Segment
///////////////////////////////////////////////////////////////////////////

/**
 * Whether `this` [MemorySegment] points to a null pointer.
 */
internal val MemorySegment.isNull: Boolean
    get() = address() == MemorySegment.NULL.address()

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