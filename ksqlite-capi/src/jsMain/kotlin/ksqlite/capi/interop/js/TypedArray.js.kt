package ksqlite.capi.interop.js

internal actual fun toInt8Array(array: ByteArray): Int8Array {
    return array.unsafeCast<Int8Array>()
}

internal actual fun toInt8Array(array: ByteArray, size: Int): Int8Array {
    if (size == array.size) {
        return toInt8Array(array)
    }

    return toInt8Array(array).subarray(0, size)
}

internal actual fun toByteArray(array: Int8Array): ByteArray {
    return array.unsafeCast<ByteArray>()
}

internal actual fun Int8Array.copyTo(
    target: ByteArray,
    targetOffset: Int
) {
    toInt8Array(target).set(this, targetOffset)
}

internal actual fun Int8Array.copyFrom(
    source: ByteArray,
    sourceOffset: Int
) {
    set(toInt8Array(source).subarray(sourceOffset))
}