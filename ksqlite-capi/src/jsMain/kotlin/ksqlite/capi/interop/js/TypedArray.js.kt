@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.interop.js

internal actual inline fun toInt8Array(array: ByteArray): Int8Array {
    return array.unsafeCast<Int8Array>()
}

internal actual inline fun toByteArray(array: Int8Array): ByteArray {
    return array.unsafeCast<ByteArray>()
}

internal actual inline fun Int8Array.copyTo(
    target: ByteArray,
    targetOffset: Int
) {
    toInt8Array(target).set(this, targetOffset)
}

internal actual inline fun Int8Array.copyFrom(
    source: ByteArray,
    sourceOffset: Int
) {
    set(toInt8Array(source).subarray(sourceOffset))
}