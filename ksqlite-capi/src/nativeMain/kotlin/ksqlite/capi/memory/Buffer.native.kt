@file:Suppress("ClassName")

package ksqlite.capi.memory

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.plus
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.posix.memcpy

public actual open class Buffer internal constructor(
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
                __dst = pinned.addressOf(destinationOffset),
                __src = pointer + sourceOffset,
                __n = size.convert()
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
                __dst = pointer + destinationOffset,
                __src = pinned.addressOf(sourceOffset),
                __n = size.convert()
            )
        }
    }

    internal companion object {

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: COpaquePointer?, size: Long): Buffer? = pointer?.let {
            Buffer(pointer.reinterpret(), size)
        }
    }
}