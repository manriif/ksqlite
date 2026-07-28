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
@file:Suppress("FunctionName", "SpellCheckingInspection", "REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.capi

import ksqlite.capi.memory.HeapAllocatorScope
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.allocateUtf8Pointer
import ksqlite.capi.memory.heapScoped
import ksqlite.foreign.Sqlite3Wasm
import ksqlite.foreign.Sqlite3WasmExports
import ksqlite.foreign.sqlite3
import ksqlite.foreign.wasm.IR
import ksqlite.foreign.wasm.WasmPointer
import ksqlite.foreign.wasm.sizeofIR
import kotlin.js.toJsBigInt

/**
 * Returns the [Sqlite3Wasm] instance.
 */
internal inline val wasm: Sqlite3Wasm
    get() = sqlite3.wasm

/**
 * Returns the [Sqlite3WasmExports] instance.
 */
internal inline val exports: Sqlite3WasmExports
    get() = wasm.exports

///////////////////////////////////////////////////////////////////////////
// Constants
///////////////////////////////////////////////////////////////////////////

internal val pointerSize = wasm.sizeofIR(IR.Ptr)

internal val SqliteTransient = (-1L).toJsBigInt()

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Returns a pointer to a string holding [text]'s content, obtained through
 * [Sqlite3WasmExports.sqlite3_mprintf].
 */
context(allocator: HeapAllocatorScope)
internal fun sqlite3_mprintf(text: String): WasmPointer =
    exports.sqlite3_mprintf(text.allocateUtf8Pointer(), NullPtr)

/**
 * Returns a pointer to a string holding [text]'s content, obtained through
 * [Sqlite3WasmExports.sqlite3_mprintf]. Returns [NullPtr] if [text] is `null`.
 */
internal fun sqlite3_mprintf(text: String?): WasmPointer {
    if (text == null) {
        return NullPtr
    }

    return heapScoped {
        sqlite3_mprintf(text)
    }
}