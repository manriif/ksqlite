package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

public actual open class StructPointer internal constructor(
    internal open val pointer: COpaquePointer
) : StructPointerBase() {

    actual override val address: Long
        get() = pointer.rawValue.toLong()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructPointer) return false

        return pointer == other.pointer
    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }
}

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

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Copies [ByteArray.size] bytes into [destination] and returns [destination].
 */
internal fun COpaquePointer.copyBytes(destination: ByteArray): ByteArray {
    destination.usePinned { pinned ->
        val _ = memcpy(
            __dst = pinned.addressOf(0),
            __src = this,
            __n = destination.size.convert()
        )
    }

    return destination
}

/**
 * Copies [count] bytes into a ByteArray and returns it.
 */
internal fun COpaquePointer.copyBytes(count: Int): ByteArray {
    return copyBytes(ByteArray(count))
}

///////////////////////////////////////////////////////////////////////////
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Reads [size] bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun CPointer<ByteVar>.toKStringFromUtf8(size: Int): String {
    return copyBytes(size).decodeToString()
}

/**
 * Reads [size] bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun COpaquePointer.toKStringFromUtf8(size: Int): String {
    return reinterpret<ByteVar>().toKStringFromUtf8(size)
}