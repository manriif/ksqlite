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
@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.allocateUtf8
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.lang.foreign.ValueLayout
import ksqlite.foreign.sqlite3 as native

internal val pointerSize = ValueLayout.ADDRESS.byteSize().toInt()

/**
 * Returns a pointer to a string holding [text]'s content, obtained through
 * [native.sqlite3_mprintf].
 */
context(allocator: SegmentAllocator)
internal fun sqlite3_mprintf(text: String): MemorySegment = native.sqlite3_mprintf
    .makeInvoker()
    .apply(text.allocateUtf8(allocator))

/**
 * Returns a pointer to a string holding [text]'s content, obtained through
 * [native.sqlite3_mprintf]. Returns [NullPtr] if [text] is `null`.
 */
internal fun sqlite3_mprintf(text: String?): MemorySegment {
    if (text == null) {
        return NullPtr
    }

    return Arena.ofConfined().use { arena ->
        with(arena) {
            sqlite3_mprintf(text)
        }
    }
}