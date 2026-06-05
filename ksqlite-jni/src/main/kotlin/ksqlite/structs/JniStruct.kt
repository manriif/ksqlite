package ksqlite.structs

import ksqlite.OutputPointer
import ksqlite.structFree
import ksqlite.structReinterpret
import java.nio.ByteBuffer

/**
 * Wrapper around a direct [ByteBuffer] pointing to a C-struct.
 * Allows reading and writing to the direct memory region where the struct resides.
 */
public abstract class JniStruct private constructor(
    private val layout: IntArray,
    private val buffer: ByteBuffer,
    public val pointer: Long
) {

    /**
     * Wraps an existing instance.
     */
    internal constructor(pointer: Long, layout: IntArray):
            this(layout, structReinterpret(pointer), pointer)

    @Suppress("unused")
    private constructor(
        layout: IntArray,
        outputPointer: OutputPointer.OfPointer,
        allocate: (OutputPointer.OfPointer) -> ByteBuffer
    ) : this(layout, allocate(outputPointer), outputPointer.value)

    /**
     * Allocates a new instance.
     */
    internal constructor(
        layout: IntArray,
        allocate: (OutputPointer.OfPointer) -> ByteBuffer
    ) : this(layout,  OutputPointer.OfPointer(0L), allocate)

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
        withOffsetAndLength(index, 1) { putInt(it, value) }
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
        withOffsetAndLength(index, 1) { putLong(it, value) }
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
        withOffsetAndLength(index, 1) { putDouble(it, value) }
    }

    /**
     * Releases the resources associated with this struct.
     */
    public fun free() {
        structFree(buffer)
    }
}