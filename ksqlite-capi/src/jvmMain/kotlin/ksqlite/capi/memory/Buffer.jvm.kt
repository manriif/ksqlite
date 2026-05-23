package ksqlite.capi.memory

import java.lang.foreign.MemorySegment

public actual class Buffer internal constructor(
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

    internal companion object {

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: MemorySegment, size: Long): Buffer? = pointer.orNull?.let {
            Buffer(pointer, size)
        }
    }
}