package ksqlite.capi.memory

import ksqlite.nativeBufferRead
import ksqlite.nativeBufferWrite

public actual class Buffer internal constructor(
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
        fun from(pointer: Long, size: Long): Buffer? = pointer.orNull?.let {
            return Buffer(pointer, size)
        }
    }
}