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

import ksqlite.capi.exports
import ksqlite.foreign.js.plus
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.structs.RawStructType
import ksqlite.structs.structSize

public actual abstract class StructLayout<S : CloseableStruct> internal constructor() :
    StructLayoutBase<S>() {

    /**
     * Type of the struct.
     */
    internal abstract val type: RawStructType

    /**
     * Creates a instance of [S] wrapping [pointer].
     */
    internal abstract fun reinterpret(pointer: WasmPointer): S
}

public actual class StructArray<S : CloseableStruct> internal constructor(
    internal val pointer: WasmPointer,
    layout: StructLayout<S>,
    elements: List<S>
) : StructArrayBase<S>(layout, elements) {

    actual override fun releaseNativeArray() {
        exports.sqlite3_free(pointer)
    }
}

public actual fun <S : CloseableStruct> StructLayout<S>.allocateArray(
    count: Int,
    initialize: S.(Int) -> Unit
): StructArray<S>? {
    val elementSize = type.structSize
    val arraySize = elementSize * count
    val arrayPointer = exports.sqlite3_malloc(arraySize).orNull ?: return null

    val elements = List(count) { index ->
        reinterpret(arrayPointer + (index * elementSize))
            .also { initialize(it, index) }
    }

    return StructArray(arrayPointer, this, elements)
}