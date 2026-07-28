/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

/**
 * Returns [Pointer] instantiated after [factory] which is passed `this` non-null pointing [Long].
 */
internal fun <Pointer : Struct> MemorySegment.wrapOrNull(factory: (MemorySegment) -> Pointer): Pointer? =
    orNull?.let(factory)

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
 * Returns an array of [count] items of type [T] obtained from [transform].
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
 * Returns an array of [count] items of type [T] obtained from [transform].
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
    toArray(count, MemorySegment::toKStringFromUtf8OrNull)

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
    this.toArray(count, MemorySegment::toKStringFromUtf8)

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun MemorySegment.toStringArrayOrEmpty(count: Int): Array<String> =
    orNull?.toStringArray(count) ?: emptyArray()

/**
 * Converts this list to an array of pointers to individually allocated structs (`const T**`),
 * allocated using [allocator]. Each element pointer is obtained through [transform].
 */
context(allocator: SegmentAllocator)
internal inline fun <T> List<T>.toCArray(
    transform: SegmentAllocator.(T) -> MemorySegment
): MemorySegment {
    val startAddress = allocator.allocate(ValueLayout.ADDRESS, size.toLong())

    forEachIndexed { index, value ->
        startAddress.setAtIndex(
            ValueLayout.ADDRESS,
            index.toLong(),
            transform(allocator, value)
        )
    }

    return startAddress
}

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a [MemorySegment], allocated using [allocator], containing the first [size] bytes of this
 * [ByteArray]. The returned [MemorySegment] can be passed to native.
 */
internal fun ByteArray.allocate(
    allocator: SegmentAllocator,
    size: Int = this.size
): MemorySegment {
    val maxSize = size.toLong()
    val destination = allocator.allocate(maxSize)
    val source = MemorySegment.ofArray(this)

    MemorySegment.copy(source, 0, destination, 0, maxSize)
    return destination
}

/**
 * Returns a [MemorySegment], allocated using [allocator], containing the first [size] bytes of this
 * [ByteArray]. The returned [MemorySegment] can be passed to native.
 */
context(allocator: SegmentAllocator)
internal fun ByteArray.allocate(size: Int = this.size): MemorySegment = allocate(allocator, size)

/**
 * Allocates a [MemorySegment], using [allocator], with a capacity of [size] bytes then invokes
 * [block] with that [MemorySegment]. The bytes of the allocated [MemorySegment] are written to this
 * [ByteArray] after [block] returns.
 */
internal inline fun <R> ByteArray.reading(
    allocator: SegmentAllocator,
    size: Int = this.size,
    block: (MemorySegment) -> R
): R {
    val pointer = allocator.allocate(size.toLong())
    val result = block(pointer)

    MemorySegment
        .ofArray(this)
        .copyFrom(pointer)

    return result
}

/**
 * Allocates a [MemorySegment], using [allocator], with a capacity of [size] bytes then invokes
 * [block] with that [MemorySegment]. The bytes of the allocated [MemorySegment] are written to this
 * [ByteArray] after [block] returns.
 */
context(allocator: SegmentAllocator)
internal inline fun <R> ByteArray.reading(
    size: Int = this.size,
    block: (MemorySegment) -> R
): R = reading(allocator, size, block)

/**
 * Reads [count] bytes from this [MemorySegment].
 */
internal fun MemorySegment.readBytes(count: Int): ByteArray =
    asSlice(0, count.toLong()).toArray(ValueLayout.JAVA_BYTE)

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the size of the null terminated string behind by this [MemorySegment], without the null
 * character.
 */
internal val MemorySegment.contentSize: Int
    get() = byteSize().toInt() - 1

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 *
 * Returns [NullPtr] if `this` is `null`.
 */
internal fun String?.allocateUtf8(allocator: SegmentAllocator): MemorySegment =
    this?.let { allocator.allocateFrom(it, Charsets.UTF_8) } ?: NullPtr

/**
 * Converts a Java string into a null-terminated C string using the UTF-8 charset, and storing
 * the result into a memory segment.
 *
 * Returns [NullPtr] if `this` is `null`.
 */
context(allocator: SegmentAllocator)
internal fun String?.allocateUtf8(): MemorySegment = allocateUtf8(allocator)

/**
 * Converts this list of Kotlin strings to C array of C strings, allocating memory for the array
 * and C strings with given [SegmentAllocator].
 */
context(allocator: SegmentAllocator)
internal fun List<String?>.toCStringArray(): MemorySegment = toCArray { it.allocateUtf8() }

/**
 * Reads and returns a String.
 *
 * If [isNullTerminated] is `false` then the string is considered non-null terminated and
 * [MemorySegment.byteSize] is used as the string length.
 */
internal fun MemorySegment.toKStringFromUtf8(isNullTerminated: Boolean = true): String {
    return try {
        when {
            !isNullTerminated -> toArray(ValueLayout.JAVA_BYTE).toString(Charsets.UTF_8)
            byteSize() == 0L -> reinterpret(Long.MAX_VALUE).getString(0, Charsets.UTF_8)
            else -> getString(0, Charsets.UTF_8)
        }
    } catch (cause: Throwable) {
        throw RuntimeException("Failed to read string from native memory", cause)
    }
}

/**
 * Reads and returns a String or returns `null` if `this` [MemorySegment] is [NullPtr].
 * If [MemorySegment.byteSize] is equals to 0 then the string is considered null terminated.
 */
internal fun MemorySegment.toKStringFromUtf8OrNull(isNullTerminated: Boolean = true): String? =
    orNull?.toKStringFromUtf8(isNullTerminated)