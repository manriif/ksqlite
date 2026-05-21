package ksqlite.capi.memory

import ksqlite.capi.callbacks.Sqlite3DestructorCallback
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout

public actual open class GenericPointer internal constructor(internal val pointer: MemorySegment)

/**
 * Memory manager that is never cleared.
 */
internal val StaticMemoryManager = MemoryManager()

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a stable [MemorySegment] to [data] available globally.
 * Returns `null` if [data] is `null`.
 *
 * The resulting reference data can be accessed using [MemoryManager.getStableRef] and it can be
 * disposed using [MemoryManager.stableRefDisposer].
 *
 * If a pointer was previously obtained using [key], it is disposed.
 */
internal fun <ClientData> MemoryManager.keyedStableRefPointer(
    key: String,
    data: Any?,
    clientData: ClientData,
    destructor: Sqlite3DestructorCallback<ClientData>? = null,
): MemorySegment = stableRefPointer(
    data = data,
    clientData = clientData,
    destructor = destructor,
    key = key
)

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
// Null
///////////////////////////////////////////////////////////////////////////

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