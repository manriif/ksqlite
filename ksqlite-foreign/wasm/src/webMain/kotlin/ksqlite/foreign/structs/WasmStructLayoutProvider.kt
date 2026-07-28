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
@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package ksqlite.foreign.structs

import ksqlite.foreign.js.plus
import ksqlite.foreign.sqlite3
import ksqlite.foreign.wasm.IR
import ksqlite.foreign.wasm.sizeofIR
import ksqlite.structs.RawStructType
import ksqlite.structs.StructLayout
import ksqlite.structs.StructLayoutProvider
import kotlin.js.toInt

/**
 * Implementation of [StructLayout] for WASM.
 */
internal object WasmStructLayoutProvider : StructLayoutProvider {

    private val structLayoutCache = mutableMapOf<RawStructType, StructLayout>()

    override fun provide(type: RawStructType): StructLayout = structLayoutCache.getOrPut(type) {
        val wasm = sqlite3.wasm
        val intSize = wasm.sizeofIR(IR.I32)
        val cLayoutSize = wasm.alloc(intSize)
        val cLayout = wasm.exports.ksqlite_struct_layout_allocate(type.value, cLayoutSize)

        check(cLayout != wasm.ptr.`null`) {
            "Failed to obtain the layout of the struct $type"
        }

        val layoutSize = wasm.peek32(cLayoutSize).toInt()

        val layout = StructLayout(layoutSize) { index ->
            wasm.peek32(cLayout + (index * intSize)).toInt()
        }

        wasm.dealloc(cLayoutSize)
        wasm.exports.ksqlite_struct_layout_free(cLayout)

        return@getOrPut layout
    }
}