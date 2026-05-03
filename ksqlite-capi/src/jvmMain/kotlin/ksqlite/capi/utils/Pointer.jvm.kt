package ksqlite.capi.utils

import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout

/**
 * Whether `this` [MemorySegment] points to a null pointer.
 */
internal val MemorySegment.isNull: Boolean
    get() = address() == MemorySegment.NULL.address()

/**
 * Returns `null` if `this` [MemorySegment] points to a null pointer.
 */
internal val MemorySegment.orNull: MemorySegment?
    get() = takeUnless { isNull }

/**
 * Returns a non-null [MemorySegment].
 */
internal val MemorySegment?.notNull: MemorySegment
    get() = this ?: MemorySegment.NULL

///////////////////////////////////////////////////////////////////////////
// Arrays
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an array of [count] item of type [T] obtained from [transform].
 */
internal inline fun <reified T> MemorySegment.toArray(
    count: Int,
    transform: (MemorySegment) -> T
): Array<T> {
    if (count == 0) {
        return emptyArray()
    }

    return Array(count) { transform(getAtIndex(ValueLayout.ADDRESS, it.toLong())) }
}

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a heap [MemorySegment] backed by the on-heap region of memory that holds the given byte
 * array.
 */
internal fun ByteArray.backing(): MemorySegment {
    return MemorySegment.ofArray(this)
}

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 *
 * Returns [MemorySegment.NULL] if `this` is `null`.
 */
context(allocator: SegmentAllocator)
internal fun String?.allocateUtf8(): MemorySegment {

    return allocator.allocateFrom(this, Charsets.UTF_8)
}

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 */
context(allocator: SegmentAllocator)
internal fun Array<String>?.allocateUtf8Array(): MemorySegment {
    if (this == null) {
        return MemorySegment.NULL
    }

    val pointers = allocator.allocate(ValueLayout.ADDRESS, size.toLong())

    forEachIndexed { index, string ->
        pointers.setAtIndex(ValueLayout.ADDRESS, index.toLong(), string.allocateUtf8())
    }

    return pointers
}

/**
 * Reads and returns a null terminated String starting from [offset].
 */
internal fun MemorySegment.getStringUtf8(offset: Long = 0): String {
    return checkNotNull(getString(offset, Charsets.UTF_8))
}

/**
 * Reads and returns a null terminated String starting from [offset] or returns `null` if `this`
 * [MemorySegment] is [MemorySegment.NULL].
 */
internal fun MemorySegment.getStringUtf8OrNull(offset: Long = 0): String? {
    if (isNull) {
        return null
    }

    return getStringUtf8(offset)
}