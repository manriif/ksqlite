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
@file:Suppress("REDUNDANT_CALL_OF_CONVERSION_METHOD")

package ksqlite.capi.memory

import ksqlite.capi.wasm
import ksqlite.foreign.js.copyFrom
import ksqlite.foreign.js.copyTo
import ksqlite.foreign.js.plus
import ksqlite.foreign.wasm.WasmMemory
import ksqlite.foreign.wasm.WasmPointer
import kotlin.js.toLong

public actual open class Buffer private constructor(
    internal val memory: WasmMemory,
    internal val pointer: WasmPointer,
    byteSize: Long
) : BufferBase(byteSize) {

    actual override val address: Long
        get() = pointer.toLong()

    actual override fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    ) {
        val begin = (pointer + sourceOffset).toLong().toInt()
        val end = begin + size

        memory.heap8()
            .subarray(begin, end)
            .copyTo(destination, destinationOffset)
    }

    actual override fun nativeWrite(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    ) {
        val begin = (pointer + destinationOffset).toLong().toInt()
        val end = begin + size

        memory.heap8()
            .subarray(begin, end)
            .copyFrom(source, sourceOffset)
    }

    internal actual companion object {

        actual val Empty = Buffer(wasm, NullPtr, 0)

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(
            pointer: WasmPointer,
            size: Long,
            memory: WasmMemory = wasm
        ): Buffer? = pointer.orNull?.let { Buffer(memory, pointer, size) }
    }
}

public actual class OpaqueBuffer private constructor(
    internal val memory: WasmMemory,
    internal val pointer: WasmPointer,
    public actual val byteSize: Long
) : AutoCloseable {

    actual override fun close() {
        try {
            memory.dealloc(pointer)
        } catch (cause: Throwable) {
            cause.printStackTrace()
        }
    }

    public actual companion object {

        public actual fun allocate(size: Long): OpaqueBuffer? {
            if (size > Int.MAX_VALUE) {
                return null
            }

            val memory = wasm
            val pointer: WasmPointer

            try {
                pointer = memory.alloc(size.toInt())
            } catch (cause: Throwable) {
                cause.printStackTrace()
                return null
            }

            return OpaqueBuffer(memory, pointer, size)
        }
    }
}