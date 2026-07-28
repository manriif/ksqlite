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
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.sizeofIR
import ksqlite.structs.Struct

/**
 * Implementation of [Struct.Adapter] for WASM, mapping pointer to [WasmPointer].
 */
internal object WasmStructAdapter : Struct.Adapter<WasmPointer> {

    override val pointerSize: Int by lazy {
        sqlite3.wasm.sizeofIR(IR.Ptr).also { size ->
            check(size == Long.SIZE_BYTES)
        }
    }

    override val nullPointer: WasmPointer
        get() = sqlite3.wasm.ptr.`null`

    override fun allocate(size: Int): Struct.Memory<WasmPointer> {
        val wasm = sqlite3.wasm
        val address = wasm.alloc(size)

        return WasmStructMemory(address, wasm)
    }

    override fun reinterpret(
        pointer: WasmPointer,
        size: Int
    ): Struct.Memory<WasmPointer> = WasmStructMemory(pointer, sqlite3.wasm)

    override fun addressAt(pointer: WasmPointer, offset: Int): WasmPointer = pointer + offset
}