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

import ksqlite.foreign.nativeReadString

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Alias to hte pointer type returned by JNI.
 */
internal typealias JniPointer = Long

/**
 * Alias to hte pointer type returned by JNI.
 */
internal val NullPtr: JniPointer
    inline get() = 0L

/**
 * Whether this long represents a null pointer.
 */
internal val JniPointer.isNull: Boolean
    inline get() = this == NullPtr

/**
 * Returns `null` if `this` [Long] points to a null pointer.
 */
internal val JniPointer.orNull: JniPointer?
    inline get() = takeUnless { isNull }

/**
 * Returns `null` if `this` [Long] points to a null pointer.
 */
internal val JniPointer?.notNull: JniPointer
    inline get() = this ?: NullPtr

/**
 * Returns [Pointer] instantiated after [factory] which is passed `this` non-null pointing [Long].
 */
internal fun <Pointer : Struct> JniPointer.wrapOrNull(factory: (Long) -> Pointer): Pointer? =
    orNull?.let(factory)

///////////////////////////////////////////////////////////////////////////
// Arrays
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an array of [LongArray.size] items of type [T] obtained from [transform].
 */
internal inline fun <reified T> LongArray.toArray(transform: (Long) -> T): Array<T> {
    if (isEmpty()) {
        return emptyArray()
    }

    return Array(size) { transform(get(it)) }
}

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Reads bytes until null termination marker is found and returns the bytes read as [String].
 */
internal fun JniPointer.toKStringFromUtf8(): String = nativeReadString(this)

/**
 * Reads bytes until null termination marker is found and returns the bytes read as [String].
 * If `this` pointer points to `null` then `null` is returned.
 */
internal fun JniPointer.toKStringFromUtf8OrNull(): String? = orNull?.toKStringFromUtf8()