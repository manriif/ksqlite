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

import ksqlite.capi.memory.VariadicValue.OfPointer
import java.lang.foreign.MemoryLayout
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Invokes a function accepting a variadic parameter.
 * String parameter are allocated within [manager].
 */
internal inline fun <Result> invokeVariadic(
    values: Array<out VariadicValue<MemorySegment>?>,
    manager: () -> MemoryManager,
    invoke: (layouts: Array<out MemoryLayout>, arguments: Array<out Any>) -> Result
): Result {
    val layouts = Array(values.size) { index ->
        when (values[index]) {
            is OfInt, is OfUInt -> ValueLayout.JAVA_INT
            is OfLong -> ValueLayout.JAVA_LONG
            is OfPointer, is OfString, null -> ValueLayout.ADDRESS
        }
    }

    val arguments = Array(values.size) { index ->
        when (val value = values[index]) {
            null -> NullPtr
            is OfUInt -> value.value.toInt()
            !is OfString -> value.value
            else -> manager().keyedStringPointer(value.key, value.value)
        }
    }

    return invoke(layouts, arguments)
}