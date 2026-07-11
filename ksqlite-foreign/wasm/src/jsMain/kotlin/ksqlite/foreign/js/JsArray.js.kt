package ksqlite.foreign.js

import js.typedarrays.Int8Array

public actual fun asInt8Array(array: ByteArray): Int8Array<*> =
    array.unsafeCast<Int8Array<*>>()

public actual fun toInt8Array(array: ByteArray, size: Int): Int8Array<*> {
    if (size == array.size) {
        return asInt8Array(array)
    }

    return asInt8Array(array).subarray(0, size)
}

public actual fun Int8Array<*>.copyTo(
    target: ByteArray,
    targetOffset: Int
) {
    asInt8Array(target).set(this, targetOffset)
}

public actual fun Int8Array<*>.copyFrom(
    source: ByteArray,
    sourceOffset: Int
) {
    set(asInt8Array(source).subarray(sourceOffset, source.size))
}