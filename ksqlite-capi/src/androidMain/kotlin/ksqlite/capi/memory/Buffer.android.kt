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
@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.capi.memory

import ksqlite.foreign.nativeBufferAllocate
import ksqlite.foreign.nativeBufferFree
import ksqlite.foreign.nativeBufferRead
import ksqlite.foreign.nativeBufferWrite
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

public actual class Buffer private constructor(
    internal val pointer: JniPointer,
    byteSize: Long
) : BufferBase(byteSize) {

    actual override val address: Long
        get() = pointer

    actual override fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    ) {
        nativeBufferRead(
            buffer = pointer,
            destination = destination,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }

    actual override fun nativeWrite(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    ) {
        nativeBufferWrite(
            buffer = pointer,
            source = source,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }

    internal actual companion object {

        actual val Empty = Buffer(0, 0)

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: Long, size: Long): Buffer? =
            pointer.orNull?.let { Buffer(pointer, size) }
    }
}

public actual class OpaqueBuffer(
    internal val pointer: JniPointer,
    public actual val byteSize: Long
) : AutoCloseable {

    private val freed = AtomicBoolean(false)

    actual override fun close() {
        if (freed.compareAndSet(expectedValue = false, newValue = true)) {
            nativeBufferFree(pointer)
        }
    }

    public actual companion object {

        public actual fun allocate(size: Long): OpaqueBuffer? {
            val pointer = nativeBufferAllocate(size).orNull ?: return null
            return OpaqueBuffer(pointer, size)
        }
    }
}