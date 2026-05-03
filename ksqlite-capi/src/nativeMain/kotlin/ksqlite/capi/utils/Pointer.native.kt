package ksqlite.capi.utils

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.get
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret

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
// Strings
///////////////////////////////////////////////////////////////////////////

/**
 * Reads [size] bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun CPointer<ByteVar>.toKStringFromUtf8(size: Int): String {
    return readBytes(size).decodeToString()
}

/**
 * Reads [size] bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun COpaquePointer.toKStringFromUtf8(size: Int): String {
    return reinterpret<ByteVar>().toKStringFromUtf8(size)
}