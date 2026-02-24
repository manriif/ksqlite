@file:Suppress("ClassName")

package ksqlite.types

import ksqlite.memory.ReadableMemoryRegion
import ksqlite.memory.WritableMemoryRegion

/**
 * Generic pointer.
 *
 * This is not an official SQLite3 type but the name is used to enforce the fact that it will be
 * managed by SQLite interface APis such as [ksqlite.sqlite3_malloc] and [ksqlite.sqlite3_free].
 *
 * It is unsafe to read a memory region that have been deallocated.
 * Developer are responsible for managing pointer they've reclaimed.
 */
public expect open class sqlite3_pointer : ReadableMemoryRegion {

    /**
     * Size of the readable memory region.
     */
    public val size: Long

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
public expect class sqlite3_mutable_pointer : sqlite3_pointer, WritableMemoryRegion {

    override fun write(
        source: ByteArray,
        size: Int,
        sourceOffset: Int,
        destinationOffset: Long
    )
}