package ksqlite.capi.memory

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandles

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Represents the null pointer.
 */
internal val NullPtr: MemorySegment
    inline get() = MemorySegment.NULL

/**
 * Whether `this` [MemorySegment] points to a null pointer.
 */
internal val MemorySegment.isNull: Boolean
    inline get() = this == NullPtr || address() == NullPtr.address()

/**
 * Returns `null` if `this` [MemorySegment] points to a null pointer.
 */
internal val MemorySegment.orNull: MemorySegment?
    inline get() = takeUnless { isNull }

/**
 * Returns a non-null [MemorySegment].
 */
internal val MemorySegment?.notNull: MemorySegment
    inline get() = this ?: NullPtr

/**
 * Sets the pointer value of `this` pointer to pointer.
 */
internal fun MemorySegment.setPointerValue(value: MemorySegment) {
    set(ValueLayout.ADDRESS, 0, value)
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
// Allocator
///////////////////////////////////////////////////////////////////////////

/**
 * Arena that is never cleared, used to allocate top level objects.
 */
internal val StaticMemoryAllocator = Arena.ofShared()

/**
 * Runs given [block] providing allocation of memory which will be automatically disposed at the end
 * of this scope.
 */
internal inline fun <T> memScoped(block: SegmentAllocator.() -> T): T =
    Arena.ofConfined().use(block)

///////////////////////////////////////////////////////////////////////////
// Functions
///////////////////////////////////////////////////////////////////////////

/**
 * Function accepting a pointer.
 */
internal fun interface ReferenceFunction {

    /**
     * Handles the [refPointer].
     */
    fun apply(refPointer: MemorySegment)
}

/**
 * Allocates a new upcall stub, that invokes [ReferenceFunction.apply] on [function].
 */
internal fun Arena.allocateReferenceFunction(function: ReferenceFunction): MemorySegment {
    val functionDescriptor = FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)

    val methodHandle = MethodHandles
        .lookup()
        .findVirtual(ReferenceFunction::class.java, "apply", functionDescriptor.toMethodType())
        .bindTo(function)

    return Linker
        .nativeLinker()
        .upcallStub(methodHandle, functionDescriptor, this)
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
internal fun MemorySegment.toNullableStringArray(count: Int): Array<String?> =
    toArray(count) { it.toKStringFromUtf8OrNull() }

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun MemorySegment.toNullableStringArrayOrEmpty(count: Int): Array<String?> =
    orNull?.toNullableStringArray(count) ?: emptyArray()

/**
 * Reads and returns an array of [count] String.
 */
internal fun MemorySegment.toStringArray(count: Int): Array<String> =
    this.toArray(count) { it.toKStringFromUtf8() }

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun MemorySegment.toStringArrayOrEmpty(count: Int): Array<String> =
    orNull?.toStringArray(count) ?: emptyArray()

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a heap [MemorySegment] backed by the on-heap region of memory that holds the given byte
 * array.
 */
internal fun ByteArray.backing(): MemorySegment = MemorySegment.ofArray(this)

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 *
 * Returns [NullPtr] if `this` is `null`.
 */
internal fun String?.allocateUtf8(allocator: SegmentAllocator): MemorySegment =
    allocator.allocateFrom(this, Charsets.UTF_8)

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 *
 * Returns [NullPtr] if `this` is `null`.
 */
context(allocator: SegmentAllocator)
internal fun String?.allocateUtf8(): MemorySegment = allocateUtf8(allocator)

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 */
context(allocator: SegmentAllocator)
internal fun Array<String>?.allocateUtf8Array(): MemorySegment {
    if (this == null) {
        return NullPtr
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
internal fun MemorySegment.toKStringFromUtf8(offset: Long = 0): String =
    checkNotNull(getString(offset, Charsets.UTF_8))

/**
 * Reads and returns a null terminated String starting from [offset] or returns `null` if `this`
 * [MemorySegment] is [NullPtr].
 */
internal fun MemorySegment.toKStringFromUtf8OrNull(offset: Long = 0): String? =
    orNull?.toKStringFromUtf8(offset)