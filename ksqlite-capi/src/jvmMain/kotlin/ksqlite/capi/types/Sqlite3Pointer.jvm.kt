@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.memory.MemoryBlock
import ksqlite.capi.memory.MemoryManager
import ksqlite.capi.memory.ReadableMemoryBlock
import ksqlite.capi.memory.WritableMemoryBlock
import ksqlite.capi.memory.isNull
import java.lang.foreign.MemorySegment

public actual open class sqlite3_pointer internal constructor(internal val block: MemoryBlock) :
    ReadableMemoryBlock by block {

    public actual val size: Long
        get() = block.blockSize

    internal companion object {

        /**
         * Returns a [sqlite3_pointer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: MemorySegment, size: Long): sqlite3_pointer? {
            if (pointer.isNull) {
                return null
            }

            return sqlite3_pointer(MemoryBlock(pointer, size))
        }
    }
}

public actual class sqlite3_mutable_pointer internal constructor(region: MemoryBlock) :
    sqlite3_pointer(region),
    WritableMemoryBlock by region {

    internal companion object {

        /**
         * Returns a [sqlite3_pointer] from [pointer] or `null` if [pointer] is `null`.
         */
        fun from(pointer: MemorySegment, size: Long): sqlite3_mutable_pointer? {
            if (pointer.isNull) {
                return null
            }

            return sqlite3_mutable_pointer(MemoryBlock(pointer, size))
        }

        /**
         * Returns a [sqlite3_mutable_pointer] from [pointer] or `null` if [pointer] is `null`.
         * The returned [sqlite3_mutable_pointer] is obtained from [MemoryManager.getStableRef].
         *
         * The reference is disposed before being returned if [dispose] is `true`.
         */
        context(manager: MemoryManager)
        fun fromStableRef(
            pointer: MemorySegment,
            dispose: Boolean = true
        ): sqlite3_mutable_pointer? {
            if (pointer.isNull) {
                return null
            }

            return manager.getStableRef(pointer).run {
                userData?.also {
                    if (dispose) {
                        dispose()
                    }
                }
            }
        }
    }
}