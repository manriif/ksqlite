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
@file:Suppress("ClassName")
@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.free
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.plus
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.posix.memcpy
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(UnsafeNumber::class)
public actual class Buffer private constructor(
    internal val pointer: CPointer<ByteVar>,
    byteSize: Long
) : BufferBase(byteSize) {

    actual override val address: Long
        get() = pointer.rawValue.toLong()

    actual override fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    ) {
        destination.usePinned { pinned ->
            val _ = memcpy(
                pinned.addressOf(destinationOffset),
                pointer + sourceOffset,
                size.convert()
            )
        }
    }

    actual override fun nativeWrite(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    ) {
        source.usePinned { pinned ->
            val _ = memcpy(
                pointer + destinationOffset,
                pinned.addressOf(sourceOffset),
                size.convert()
            )
        }
    }

    internal actual companion object {

        private val ZeroByte = nativeHeap.alloc(0.toByte())
        actual val Empty = Buffer(ZeroByte.ptr, 0)

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: COpaquePointer?, size: Long): Buffer? =
            pointer?.let { Buffer(pointer.reinterpret(), size) }
    }
}

public actual class OpaqueBuffer private constructor(
    internal val pointer: CPointer<ByteVar>,
    public actual val byteSize: Long
) : AutoCloseable {

    private val freed = AtomicBoolean(false)

    public actual override fun close() {
        if (freed.compareAndSet(expectedValue = false, newValue = true)) {
            nativeHeap.free(pointer)
        }
    }

    public actual companion object {

        public actual fun allocate(size: Long): OpaqueBuffer? {
            val pointer: CPointer<ByteVar>

            try {
                pointer = nativeHeap
                    .alloc(size, 1)
                    .reinterpret<ByteVar>()
                    .ptr
            } catch (cause: Throwable) {
                cause.printStackTrace()
                return null
            }

            return OpaqueBuffer(pointer, size)
        }
    }
}