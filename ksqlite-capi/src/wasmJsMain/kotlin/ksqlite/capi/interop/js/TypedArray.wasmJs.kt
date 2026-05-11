@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi.interop.js

@IgnorableReturnValue
private inline fun Int8Array.fill(getValue: (index: Int) -> Byte): Int8Array {
    for (index in 0 until length) {
        this[index] = getValue(index).toInt().toJsNumber()
    }

    return this
}

internal actual inline fun toInt8Array(array: ByteArray): Int8Array {
    return Int8Array(length = array.size).fill(array::get)
}

internal actual inline fun Int8Array.copyTo(
    target: ByteArray,
    targetOffset: Int
) {
    for (index in 0 until length) {
        target[index + targetOffset] = this[index].toInt().toByte()
    }
}

internal actual inline fun Int8Array.copyFrom(
    source: ByteArray,
    sourceOffset: Int
) {
    fill { index ->
        source[index + sourceOffset]
    }
}