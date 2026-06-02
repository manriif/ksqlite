package ksqlite.capi.memory

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout

public actual open class StructPointer internal constructor(
    internal val pointer: MemorySegment,
    private val arena: Arena? = null
) : StructPointerBase() {

    internal constructor(
        arena: Arena = Arena.ofShared(),
        allocate: Arena.() -> MemorySegment
    ) : this(arena.allocate(), arena)

    actual override val address: Long
        get() = pointer.address()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructPointer) return false

        return pointer == other.pointer
    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }

    internal fun free() {
        arena?.close()
    }
}

/**
 * Memory manager that is never cleared.
 */
internal val StaticMemoryManager = MemoryManager()

///////////////////////////////////////////////////////////////////////////
// Allocator
///////////////////////////////////////////////////////////////////////////

/**
 * Runs given [block] providing allocation of memory which will be automatically disposed at the end
 * of this scope.
 */
internal inline fun <T> memScoped(block: SegmentAllocator.() -> T): T {
    return Arena.ofConfined().use(block)
}

///////////////////////////////////////////////////////////////////////////
// Segment
///////////////////////////////////////////////////////////////////////////

/**
 * Whether `this` [MemorySegment] points to a null pointer.
 */
internal val MemorySegment.isNull: Boolean
    get() = this == MemorySegment.NULL || address() == MemorySegment.NULL.address()

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

/**
 * Sets the pointer value of `this` pointer to pointer.
 */
internal fun MemorySegment.setPointer(pointer: MemorySegment) {
    set(ValueLayout.ADDRESS, 0, pointer)
}

/**
 * Sets the [Long] value of `this` pointer to long.
 */
internal fun MemorySegment.setValue(value: Long) {
    set(ValueLayout.JAVA_LONG, 0, value)
}

/**
 * Sets the [Int] value of `this` pointer to int.
 */
internal fun MemorySegment.setValue(value: Int) {
    set(ValueLayout.JAVA_INT, 0, value)
}

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

/**
 * Returns an array of [count] item of type [T] obtained from [transform].
 * Returns an empty array if `this` is `null`.
 */
internal inline fun <reified T> MemorySegment.toArrayOrEmpty(
    count: Int,
    transform: (MemorySegment) -> T
): Array<T> = orNull?.toArray(count, transform) ?: emptyArray()

/**
 * Reads and returns an array of [count] String.
 */
internal fun MemorySegment.toNullableStringArray(count: Int): Array<String?> {
    return toArray(count) { it.toKStringFromUtf8OrNull() }
}

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun MemorySegment.toNullableStringArrayOrEmpty(count: Int): Array<String?> {
    return orNull?.toNullableStringArray(count) ?: emptyArray()
}

/**
 * Reads and returns an array of [count] String.
 */
internal fun MemorySegment.toStringArray(count: Int): Array<String> {
    return this.toArray(count) { it.toKStringFromUtf8() }
}

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun MemorySegment.toStringArrayOrEmpty(count: Int): Array<String> {
    return orNull?.toStringArray(count) ?: emptyArray()
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
internal fun String?.allocateUtf8(allocator: SegmentAllocator): MemorySegment {
    return allocator.allocateFrom(this, Charsets.UTF_8)
}

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 *
 * Returns [MemorySegment.NULL] if `this` is `null`.
 */
context(allocator: SegmentAllocator)
internal fun String?.allocateUtf8(): MemorySegment {
    return allocateUtf8(allocator)
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
internal fun MemorySegment.toKStringFromUtf8(offset: Long = 0): String {
    return checkNotNull(getString(offset, Charsets.UTF_8))
}

/**
 * Reads and returns a null terminated String starting from [offset] or returns `null` if `this`
 * [MemorySegment] is [MemorySegment.NULL].
 */
internal fun MemorySegment.toKStringFromUtf8OrNull(offset: Long = 0): String? {
    if (isNull) {
        return null
    }

    return toKStringFromUtf8(offset)
}