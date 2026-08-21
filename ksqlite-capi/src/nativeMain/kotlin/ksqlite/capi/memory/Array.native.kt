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
package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.NativePtr
import kotlinx.cinterop.convert
import kotlinx.cinterop.sizeOf
import ksqlite.foreign.ksqlite_cipher_params
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_malloc64

public interface KotlinNativeStructLayout

public actual abstract class StructLayout<S : CloseableStruct> internal constructor() :
    StructLayoutBase<S>() {

    /**
     * Returns the size of an element.
     */
    public abstract val elementSize: Long

    /**
     * Creates a instance of [S] wrapping [rawPtr].
     */
    internal abstract fun reinterpret(rawPtr: NativePtr): S
}

public actual class StructArray<S : CloseableStruct> internal constructor(
    internal val pointer: COpaquePointer,
    layout: StructLayout<S>,
    elements: List<S>
) : StructArrayBase<S>(layout, elements) {

    actual override fun releaseNativeArray() {
        sqlite3_free(pointer)
    }
}

public actual fun <S : CloseableStruct> StructLayout<S>.allocateArray(
    count: Int,
    initialize: S.(Int) -> Unit
): StructArray<S>? {
    val elementSize = sizeOf<ksqlite_cipher_params>()
    val arraySize = elementSize * count.toLong()
    val arrayPointer = sqlite3_malloc64(arraySize.convert()) ?: return null

    val elements = List(count) { index ->
        reinterpret(arrayPointer.rawValue + elementSize * index)
            .also { initialize(it, index) }
    }

    return StructArray(arrayPointer, this, elements)
}