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