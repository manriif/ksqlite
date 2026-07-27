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

import java.lang.foreign.MemorySegment

public actual class Buffer private constructor(
    internal val pointer: MemorySegment,
    byteSize: Long
) : BufferBase(byteSize) {

    actual override val address: Long
        get() = pointer.address()

    actual override fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    ) {
        MemorySegment.copy(
            pointer,
            sourceOffset,
            MemorySegment.ofArray(destination),
            destinationOffset.toLong(),
            size.toLong()
        )
    }

    actual override fun nativeWrite(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    ) {
        MemorySegment.copy(
            MemorySegment.ofArray(source),
            sourceOffset.toLong(),
            pointer,
            destinationOffset,
            size.toLong()
        )
    }

    internal actual companion object {

        actual val Empty = Buffer(NullPtr, 0)

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: MemorySegment, size: Long): Buffer? =
            pointer.orNull?.let { Buffer(pointer, size) }
    }
}