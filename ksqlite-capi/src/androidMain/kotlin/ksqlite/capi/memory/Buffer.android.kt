package ksqlite.capi.memory

import ksqlite.nativeBufferRead
import ksqlite.nativeBufferWrite
import ksqlite.requireBuffer

public actual class Buffer internal constructor(
    internal val pointer: JniPointer,
    byteSize: Long
) : BufferBase(byteSize) {

    internal val buffer by lazy {
        requireBuffer(pointer, byteSize)
    }

    actual override val address: Long
        get() = pointer

    actual override fun nativeRead(
        destination: ByteArray,
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int
    ) {
        nativeBufferRead(
            buffer = buffer,
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
            buffer = buffer,
            source = source,
            size = size,
            sourceOffset = sourceOffset,
            destinationOffset = destinationOffset
        )
    }

    internal companion object {

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: Long, size: Long): Buffer? = pointer.orNull?.let {
            return Buffer(pointer, size)
        }
    }
}