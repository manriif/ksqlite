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

import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.NullPtr
import ksqlite.capi.memory.isNull
import ksqlite.foreign.JniPointer
import ksqlite.foreign.OutputPointer
import ksqlite.foreign.nativeFreeAndMalloc
import kotlin.reflect.KMutableProperty0

/**
 * Allocates a new string for [message] (if != `null`) using `sqlite3_mprintf` and deallocates
 * [property]'s current one (if != [NullPtr]), then set the address of the allocated content to
 * [property].
 */
internal fun sqlite3_mprintf(property: KMutableProperty0<JniPointer>, message: String?) {
    val oldPointer = property.get()

    if (message != null || !oldPointer.isNull) {
        property.set(nativeFreeAndMalloc(oldPointer, message))
    }
}

internal inline fun toBuffer(block: (OutputPointer.OfInt64) -> Long): Buffer? {
    val size = OutputPointer.OfInt64(0)
    val pointer = block(size)
    return Buffer.from(pointer, size.value)
}