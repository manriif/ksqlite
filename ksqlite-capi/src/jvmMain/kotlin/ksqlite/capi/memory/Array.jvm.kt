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

import ksqlite.foreign.sqlite3
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment

public actual abstract class StructLayout<S : CloseableStruct> internal constructor() :
    StructLayoutBase<S>() {

    /**
     * Layout of the struct.
     */
    internal abstract val layout: MemoryLayout

    /**
     * Creates a instance of [S] wrapping [pointer].
     */
    internal abstract fun reinterpret(pointer: MemorySegment): S
}

public actual class StructArray<S : CloseableStruct> internal constructor(
    internal val pointer: MemorySegment,
    layout: StructLayout<S>,
    elements: List<S>
) : StructArrayBase<S>(layout, elements) {

    actual override fun releaseNativeArray() {
        sqlite3.sqlite3_free(pointer)
    }
}

public actual fun <S : CloseableStruct> StructLayout<S>.allocateArray(
    count: Int,
    initialize: S.(Int) -> Unit
): StructArray<S>? {
    val elementSize = layout.byteSize()
    val arraySize = elementSize * count.toLong()
    val arrayPointer = sqlite3.sqlite3_malloc64(arraySize).orNull ?: return null
    val resizedArrayPointer = arrayPointer.reinterpret(arraySize)

    val elements = List(count) { index ->
        reinterpret(resizedArrayPointer.asSlice(index * elementSize, layout))
            .also { initialize(it, index) }
    }

    return StructArray(arrayPointer, this, elements)
}