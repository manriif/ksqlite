@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.ReadableMemoryBlock
import ksqlite.capi.memory.WritableMemoryBlock

public abstract class sqlite3_pointer_base {

    /**
     * Native address.
     */
    protected abstract val address: Long

    /**
     * Size of the memory region.
     */
    public abstract val size: Long

    override fun toString(): String {
        return "sqlite3_pointer(address=$address, size=$size)"
    }
}

/**
 * Generic pointer.
 *
 * This is not an official SQLite3 type but the name is used to enforce the fact that it will be
 * managed by SQLite interface APis such as [ksqlite.capi.sqlite3_malloc] and [ksqlite.capi.sqlite3_free].
 *
 * It is unsafe to read a memory region that have been deallocated.
 * Developer are responsible for managing pointer they've reclaimed.
 */
public expect open class sqlite3_pointer: sqlite3_pointer_base, ReadableMemoryBlock {

    override val address: Long
    override val size: Long

    override fun read(
        size: Int,
        sourceOffset: Long,
        destinationOffset: Int,
        destination: ByteArray
    ): ByteArray
}

/**
 * [sqlite3_pointer] which can be written.
 *
 * It is unsafe to write a memory region that have been deallocated.
 */
public expect class sqlite3_mutable_pointer : sqlite3_pointer, WritableMemoryBlock {

    override fun write(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    )
}