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

/**
 * Base for all [StructLayout] implementations.
 */
public abstract class StructLayoutBase<S : CloseableStruct> {

    /**
     * Cleanups the given [instance].
     */
    internal abstract fun cleanup(instance: S)
}

public expect abstract class StructLayout<S : CloseableStruct> : StructLayoutBase<S>

/**
 * Base for all [StructArray] implementations.
 */
public abstract class StructArrayBase<S : CloseableStruct> internal constructor(
    private val layout: StructLayout<S>,
    private val elements: List<S>
) : AutoCloseable,
    Iterable<S> {

    /**
     * Returns the number of elements in this array.
     */
    public val size: Int
        get() = elements.size

    /**
     * Returns the element at [index].
     */
    public operator fun get(index: Int): S = elements[index]

    /**
     * Returns an iterator over [S]s.
     */
    public override fun iterator(): Iterator<S> = elements.iterator()

    /**
     * Releases the native array.
     */
    internal abstract fun releaseNativeArray()

    /**
     * Releases the native array and all its elements.
     */
    override fun close() {
        elements.forEach(layout::cleanup)
        releaseNativeArray()
    }
}

/**
 * Contiguous native array of structs (`S[]` / `const S*`)
 * It is not recommended to close an individual element.
 */
public expect class StructArray<S : CloseableStruct> : StructArrayBase<S> {
    override fun releaseNativeArray()
}

/**
 * Allocates a contiguous array, composed of [count] element of type [S].
 *
 * The allocated memory isn't guaranteed to be zeroized, so it is important to [initialize] all the
 * members of a given instance [S].
 *
 * If the allocation fails because not enough memory is available, then null is returned instead.
 */
public expect fun <S : CloseableStruct> StructLayout<S>.allocateArray(
    count: Int,
    initialize: S.(Int) -> Unit
): StructArray<S>?