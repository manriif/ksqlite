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
package ksqlite.kapi.buffer

import ksqlite.capi.memory.read
import ksqlite.capi.memory.readBytes
import ksqlite.capi.memory.readBytesOrThrow
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.capi.memory.ReadableBuffer as CapiReadableBuffer

/**
 * Region of native memory that can be read.
 */
public open class ReadableBuffer internal constructor(
    internal open val buffer: CapiReadableBuffer,
    scope: CloseableScope? = null,
) {

    @Suppress("CanBePrimaryConstructorProperty")
    internal open val scope: CloseableScope? = scope

    /**
     * Size of the memory region in bytes.
     */
    public val byteSize: Long
        get() = scope?.notClosed(block = buffer::byteSize) ?: buffer.byteSize

    /**
     * Reads [size] bytes from the native memory block into [destination].
     *
     * The read starts at [sourceOffset] in the native memory region and writes into [destination]
     * starting at [destinationOffset].
     *
     * @throws IllegalArgumentException if [size], [sourceOffset], or [destinationOffset] is
     * negative.
     * @throws IndexOutOfBoundsException if the requested range is out of bounds in either the
     * native memory block or [destination].
     */
    public fun read(
        destination: ByteArray,
        size: Int = destination.size,
        sourceOffset: Long = 0,
        destinationOffset: Int = 0
    ) {
        scope?.ensureNotClosed()

        buffer.read(
            destination = destination,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }
}

///////////////////////////////////////////////////////////////////////////
// Extensions
///////////////////////////////////////////////////////////////////////////

/**
 * Reads [size] bytes from the native memory block and returns a [ByteArray] holding them.
 *
 * The read starts at [offset] in the native memory region and writes into the returned [ByteArray].
 *
 * @throws IllegalArgumentException if [size] or [offset] is negative.
 * @throws IndexOutOfBoundsException if the requested range is out of bounds in either the
 * native memory block or the returned [ByteArray].
 */
public fun ReadableBuffer.read(
    size: Int,
    offset: Long = 0
): ByteArray {
    scope?.ensureNotClosed()
    return buffer.read(size, offset)
}

/**
 * Reads at most [Int.MAX_VALUE] bytes from `this` buffer.
 */
public fun ReadableBuffer.readBytes(): ByteArray {
    scope?.ensureNotClosed()
    return buffer.readBytes()
}

/**
 * Reads all bytes from `this` buffer.
 *
 * @throws UnsupportedOperationException if not all bytes in `this` buffer can fit into a
 * [ByteArray].
 */
public fun ReadableBuffer.readBytesOrThrow(): ByteArray {
    scope?.ensureNotClosed()
    return buffer.readBytesOrThrow()
}