@file:OptIn(ExperimentalAtomicApi::class)

package ksqlite.capi.memory

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

public actual class Buffer private constructor(
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

    internal actual companion object {

        actual val Empty = Buffer(NullPtr, 0)

        /**
         * Returns a [Buffer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: MemorySegment, size: Long): Buffer? =
            pointer.orNull?.let { Buffer(pointer, size) }
    }
}

public actual class OpaqueBuffer private constructor(
    private val arena: Arena,
    internal val pointer: MemorySegment,
) : AutoCloseable {

    private val freed = AtomicBoolean(false)

    public actual val byteSize: Long
        get() = pointer.byteSize()

    public actual override fun close() {
        if (freed.compareAndSet(expectedValue = false, newValue = true)) {
            try {
                arena.close()
            } catch (cause: Throwable) {
                cause.printStackTrace()
            }
        }
    }

    public actual companion object {

        public actual fun allocate(size: Long): OpaqueBuffer? {
            val arena = Arena.ofShared()
            val pointer: MemorySegment

            try {
                pointer = arena.allocate(size)
            } catch (cause: Throwable) {
                cause.printStackTrace()
                arena.close()
                return null
            }

            return OpaqueBuffer(arena, pointer)
        }
    }
}