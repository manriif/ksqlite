package ksqlite.capi.interop.js

@IgnorableReturnValue
private inline fun Int8Array.fill(getValue: (index: Int) -> Byte): Int8Array {
    for (index in 0 until length) {
        this[index] = getValue(index).toInt().toJsNumber()
    }

    return this
}

internal actual fun toInt8Array(array: ByteArray, size: Int): Int8Array {
    return Int8Array(length = size).fill(array::get)
}

internal actual fun toInt8Array(array: ByteArray): Int8Array {
    return toInt8Array(array, array.size)
}

internal actual fun toByteArray(array: Int8Array): ByteArray {
    return ByteArray(size = array.length) { index ->
        array[index].toInt().toByte()
    }
}

internal actual fun Int8Array.copyTo(
    target: ByteArray,
    targetOffset: Int
) {
    for (index in 0 until length) {
        target[index + targetOffset] = this[index].toInt().toByte()
    }
}

internal actual fun Int8Array.copyFrom(
    source: ByteArray,
    sourceOffset: Int
) {
    fill { index ->
        source[index + sourceOffset]
    }
}