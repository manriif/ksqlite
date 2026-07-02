package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.convert
import kotlinx.cinterop.pointed
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toLong
import ksqlite.foreign.sqlite3_free
import ksqlite.foreign.sqlite3_malloc64

public actual open class Struct internal constructor(internal open val pointer: COpaquePointer) :
    StructBase() {

    actual override val address: Long
        get() = pointer.toLong()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Struct) return false

        return pointer == other.pointer
    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }
}

public actual open class AllocatableStruct internal constructor(
    pointer: COpaquePointer,
    private val owned: Boolean = false
) : Struct(pointer),
    AutoCloseable {

    public actual override fun close() {
        if (owned) {
            sqlite3_free(pointer)
        }
    }

    internal companion object {

        /**
         * Allocates [S] using SQLite's allocator.
         */
        protected inline fun <reified S : CVariable> allocate(size: Long? = null): CPointer<S> {
            val defaultSize = sizeOf<S>()
            val retainedSize = size ?: defaultSize

            check(retainedSize >= defaultSize) {
                "Allocation size must be greater than or equals to the struct size"
            }

            return checkNotNull(sqlite3_malloc64(retainedSize.convert())) {
                "Failed to allocate an instance of ${S::class}"
            }.reinterpret()
        }

        /**
         * Allocates [S] using SQLite's allocator and invokes [configure] with the allocated [S]
         * as receiver.
         */
        protected inline fun <reified S : CVariable> allocate(
            size: Long? = null,
            configure: S.() -> Unit
        ): CPointer<S> = allocate<S>(size).apply {
            pointed.configure()
        }
    }
}