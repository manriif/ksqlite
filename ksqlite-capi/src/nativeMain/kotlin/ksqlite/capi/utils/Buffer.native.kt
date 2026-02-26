package ksqlite.capi.utils

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret

/**
 * Reads [size] bytes from this pointer as [ByteArray] and then convert to string.
 */
internal fun COpaquePointer.toKStringFromUtf8(size: Int): String {
    return reinterpret<ByteVar>().readBytes(size).decodeToString()
}