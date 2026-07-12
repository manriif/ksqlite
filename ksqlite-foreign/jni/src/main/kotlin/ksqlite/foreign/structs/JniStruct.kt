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
package ksqlite.foreign.structs

import ksqlite.foreign.OutputPointer
import ksqlite.foreign.structFree
import ksqlite.foreign.structMalloc
import ksqlite.foreign.structReinterpret
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Wrapper around a direct [ByteBuffer] pointing to a C-struct.
 * Allows reading and writing to the direct memory region where the struct resides.
 */
public abstract class JniStruct private constructor(
    private val layout: IntArray,
    buffer: ByteBuffer,
    public val pointer: Long,
    size: Int,
) {

    init {
        require(size >= layout.last()) {
            "Allocation size must not be less than the struct layout size"
        }
    }

    private val buffer = buffer.order(ByteOrder.nativeOrder())

    /**
     * Wraps an existing instance.
     */
    internal constructor(
        layout: IntArray,
        pointer: Long,
        size: Int = layout.last()
    ) : this(layout, structReinterpret(size, pointer), pointer, size)

    /**
     * Allocates a new instance.
     */
    internal constructor(
        layout: IntArray,
        size: Int = layout.last(),
        outputPointer: OutputPointer.OfPointer = OutputPointer.OfPointer(0L)
    ) : this(layout, structMalloc(size, outputPointer), outputPointer.value, size)

    /**
     * Invokes [block] passing it the offset of the field at [index].
     * It is checked that the field length matches [expectedLength].
     *
     * If `null` is returned then allocation may have failed.
     */
    @IgnorableReturnValue
    private inline fun <R> withOffsetAndLength(
        index: Int,
        expectedLength: Int,
        block: ByteBuffer.(offset: Int) -> R
    ): R? {
        if (pointer == 0L) {
            // Allocation failed, potentially out of memory
            return null
        }

        val offset = layout[index * 2]
        val length = layout[index * 2 + 1]

        check(length == expectedLength) {
            "Trying to access a $length bytes field but $expectedLength bytes were expected"
        }

        return buffer.block(offset)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Primitives
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Reads one byte starting from the offset of the field at [index].
     */
    protected fun readByte(index: Int): Byte =
        withOffsetAndLength(index, 1, ByteBuffer::get) ?: 0

    /**
     * Writes one byte [value] at the offset of the field at [index].
     */
    protected fun writeByte(index: Int, value: Byte) {
        withOffsetAndLength(index, 1) { put(it, value) }
    }

    /**
     * Reads four bytes starting from the offset of the field at [index].
     */
    protected fun readInt(index: Int): Int =
        withOffsetAndLength(index, 4, ByteBuffer::getInt) ?: 0

    /**
     * Writes four bytes [value] at the offset of the field at [index].
     */
    protected fun writeInt(index: Int, value: Int) {
        withOffsetAndLength(index, 4) { putInt(it, value) }
    }

    /**
     * Reads eight bytes starting from the offset of the field at [index].
     */
    protected fun readLong(index: Int): Long =
        withOffsetAndLength(index, 8, ByteBuffer::getLong) ?: 0L

    /**
     * Writes eight bytes [value] at the offset of the field at [index].
     */
    protected fun writeLong(index: Int, value: Long) {
        withOffsetAndLength(index, 8) { putLong(it, value) }
    }

    /**
     * Reads eight bytes starting from the offset of the field at [index].
     */
    protected fun readDouble(index: Int): Double =
        withOffsetAndLength(index, 8, ByteBuffer::getDouble) ?: .0

    /**
     * Writes eight bytes [value] at the offset of the field at [index].
     */
    protected fun writeDouble(index: Int, value: Double) {
        withOffsetAndLength(index, 8) { putDouble(it, value) }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Arrays
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the address of the item at index [itemIndex] of the array for which the first item
     * index is located at [baseAddress]. The item size is obtained from [itemLayout].
     */
    protected fun arrayItemAddress(
        baseAddress: Long,
        itemIndex: Int,
        itemLayout: IntArray
    ): Long = baseAddress + itemIndex * itemLayout.last()

    ///////////////////////////////////////////////////////////////////////////
    // Memory
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Releases the resources associated with this struct.
     */
    public open fun free() {
        structFree(buffer)
    }
}