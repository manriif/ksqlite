package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValues
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.toKStringFromUtf8
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

///////////////////////////////////////////////////////////////////////////
// Arrays
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an array of [count] item of type [T] [transform]ed from pointer [P].
 */
internal inline fun <P : CPointer<*>, reified T> CPointer<CPointerVarOf<P>>.toArray(
    count: Int,
    transform: (P?) -> T
): Array<T> {
    if (count == 0) {
        return emptyArray()
    }

    return Array(count) { transform(get(it)) }
}

/**
 * Returns an array of [count] item of type [T] [transform]ed from pointer [P].
 * Returns an empty array if `this` is `null`.
 */
internal inline fun <P : CPointer<*>, reified T> CPointer<CPointerVarOf<P>>?.toArrayOrEmpty(
    count: Int,
    transform: (P?) -> T
): Array<T> {
    if (this == null) {
        return emptyArray()
    }

    return toArray(count, transform)
}

/**
 * Reads and returns an array of [count] String.
 */
internal fun CPointer<CPointerVar<ByteVar>>.toNullableStringArray(count: Int): Array<String?> =
    this.toArray(count) { it?.toKStringFromUtf8() }

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun CPointer<CPointerVar<ByteVar>>?.toNullableStringArrayOrEmpty(count: Int): Array<String?> =
    this?.toNullableStringArray(count) ?: emptyArray()

/**
 * Reads and returns an array of [count] String.
 */
internal fun CPointer<CPointerVar<ByteVar>>.toStringArray(count: Int): Array<String> =
    this.toArray(count) { it!!.toKStringFromUtf8() }

/**
 * Reads and returns an array of [count] String.
 * Returns an empty array if `this` is `null`.
 */
internal fun CPointer<CPointerVar<ByteVar>>?.toStringArrayOrEmpty(count: Int): Array<String> =
    this?.toStringArray(count) ?: emptyArray()

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Copies [ByteArray.size] bytes into [destination] and returns [destination].
 */
@OptIn(UnsafeNumber::class)
internal fun COpaquePointer.copyBytes(destination: ByteArray): ByteArray {
    destination.usePinned { pinned ->
        val _ = memcpy(
            pinned.addressOf(0),
            this,
            destination.size.convert()
        )
    }

    return destination
}

/**
 * Copies [count] bytes into a ByteArray and returns it.
 */
internal fun COpaquePointer.copyBytes(count: Int): ByteArray =
    copyBytes(ByteArray(count))

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the size of the null terminated string behind by this [CValues], without the null
 * character.
 */
internal val CValues<ByteVar>.contentSize: Int
    inline get() = size - 1

/**
 * Reads [size] bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun CPointer<ByteVar>.toKStringFromUtf8(size: Int): String =
    copyBytes(size).decodeToString()

/**
 * Reads [size] bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun COpaquePointer.toKStringFromUtf8(size: Int): String =
    reinterpret<ByteVar>().toKStringFromUtf8(size)

/**
 * Reads bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun COpaquePointer.toKStringFromUtf8(): String =
    reinterpret<ByteVar>().toKStringFromUtf8()