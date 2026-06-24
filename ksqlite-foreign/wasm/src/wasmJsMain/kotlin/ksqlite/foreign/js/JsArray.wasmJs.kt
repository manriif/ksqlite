package ksqlite.foreign.js

import js.buffer.ArrayBuffer
import js.numbers.JsNumbers.toJsByte
import js.typedarrays.Int8Array
import js.typedarrays.toInt8Array

@IgnorableReturnValue
private inline fun Int8Array<*>.fill(getValue: (index: Int) -> Byte): Int8Array<*> {
    for (index in 0 until length) {
        this[index] = getValue(index).toJsByte()
    }

    return this
}

public actual fun asInt8Array(array: ByteArray): Int8Array<*> = array.toInt8Array()

public actual fun toInt8Array(array: ByteArray, size: Int): Int8Array<*>  =
    Int8Array<ArrayBuffer>(length = size).fill(array::get)

public actual fun Int8Array<*>.copyTo(
    target: ByteArray,
    targetOffset: Int
) {
    for (index in 0 until length) {
        target[index + targetOffset] = this[index].toInt().toByte()
    }
}

public actual fun Int8Array<*>.copyFrom(
    source: ByteArray,
    sourceOffset: Int
) {
    fill { index ->
        source[index + sourceOffset]
    }
}