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
package ksqlite.kapi.helpers

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.memory.OutputParam
import kotlin.jvm.JvmName

/**
 * Invokes [block] and returns [param]'s value if [block] returns without throwing.
 */
internal inline fun <V : Any, P : OutputParam<V>> usingParam(
    param: P,
    block: (P) -> Unit
): V {
    block(param)
    return param.value
}

/**
 * Invokes [block] and returns [param]'s value if [block] returns without throwing.
 */
@JvmName("usingParam2")
internal inline fun <V : Any, P : OutputParam<V?>> usingParam(
    param: P,
    block: (P) -> Unit
): V {
    block(param)
    return checkNotNull(param.value) { "Parameter's value is null" }
}

/**
 * Invokes [block] and returns a [Boolean] if [block] returns without throwing.
 */
internal inline fun usingBooleanParam(
    initialValue: Boolean?,
    block: (Int32OutputParam) -> Unit
): Boolean {
    val param = Int32OutputParam(
        when (initialValue) {
            false -> 0
            true -> 1
            null -> -1
        }
    )

    block(param)

    return when (val value = param.value) {
        0 -> false
        1 -> true
        else -> error("Expected SQLite to write 0 or 1 to 32-bit integer but $value was found")
    }
}

/**
 * Invokes [block] and returns [R], resulting from [transform] which is passed [param1]'s [V1] and
 * [param2]'s [V2], if [block] returns without throwing.
 */
internal inline fun <R, V1 : Any, P1 : OutputParam<V1>, V2 : Any, P2 : OutputParam<V2>> usingParams(
    param1: P1,
    param2: P2,
    transform: (V1, V2) -> R,
    block: (P1, P2) -> Unit
): R {
    block(param1, param2)
    return transform(param1.value, param2.value)
}

/**
 * Invokes [block] and returns [R], resulting from [transform] which is passed [param1]'s [V1] and
 * [param2]'s [V2], if [block] returns without throwing.
 */
@JvmName("usingParams2")
internal inline fun <R, V1 : Any, P1 : OutputParam<V1?>, V2 : Any, P2 : OutputParam<V2>> usingParams(
    param1: P1,
    param2: P2,
    transform: (V1, V2) -> R,
    block: (P1, P2) -> Unit
): R {
    block(param1, param2)

    return transform(
        checkNotNull(param1.value) { "First parameter's value is null" },
        param2.value
    )
}

/**
 * Invokes [block] and returns [R], resulting from [transform] which is passed [param1]'s [V1] and
 * [param2]'s [V2], if [block] returns without throwing.
 */
@JvmName("usingParams3")
internal inline fun <R, V1 : Any, P1 : OutputParam<V1?>, V2 : Any, P2 : OutputParam<V2?>> usingParams(
    param1: P1,
    param2: P2,
    transform: (V1, V2) -> R,
    block: (P1, P2) -> Unit
): R {
    block(param1, param2)

    return transform(
        checkNotNull(param1.value) { "First parameter's value is null" },
        checkNotNull(param2.value) { "Second parameter's value is null" }
    )
}