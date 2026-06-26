@file:Suppress("ClassName")

package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.plus
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

@OptIn(UnsafeNumber::class)
public actual class Buffer internal constructor(
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

        private val OneByte = nativeHeap.alloc(0.toByte())
        actual val Empty = Buffer(OneByte.ptr, 0)

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: COpaquePointer?, size: Long): Buffer? = pointer?.let {
            Buffer(pointer.reinterpret(), size)
        }
    }
}