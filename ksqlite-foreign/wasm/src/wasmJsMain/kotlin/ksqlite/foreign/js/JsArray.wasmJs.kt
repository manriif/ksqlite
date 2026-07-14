/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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