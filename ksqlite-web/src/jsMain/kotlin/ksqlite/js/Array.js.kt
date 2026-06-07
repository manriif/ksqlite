package ksqlite.js

import js.typedarrays.Int8Array

public actual fun toInt8Array(array: ByteArray, size: Int): Int8Array<*> {
    if (size == array.size) {
        return toInt8Array(array)
    }

    return toInt8Array(array).subarray(0, size)
}

public actual fun Int8Array<*>.copyTo(
    target: ByteArray,
    targetOffset: Int
) {
    toInt8Array(target).set(this, targetOffset)
}

public actual fun Int8Array<*>.copyFrom(
    source: ByteArray,
    sourceOffset: Int
) {
    set(toInt8Array(source).subarray(sourceOffset, source.size))
}