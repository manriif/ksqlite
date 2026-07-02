package ksqlite.capi.memory

import ksqlite.foreign.sqlite3.sqlite3_free
import ksqlite.foreign.sqlite3.sqlite3_malloc64
import java.lang.foreign.Arena
import java.lang.foreign.GroupLayout
import java.lang.foreign.MemorySegment

public actual open class Struct internal constructor(internal val pointer: MemorySegment) :
    StructBase() {

    actual override val address: Long
        get() = pointer.address()

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
    pointer: MemorySegment,
    private val arena: Arena?,
    protected val struct: MemorySegment // reinterpreted
) : Struct(pointer),
    AutoCloseable {

    private constructor(
        layout: GroupLayout,
        arena: Arena,
        pointer: MemorySegment
    ) : this(pointer, arena, pointer.reinterpret(layout.byteSize(), arena, null))

    internal constructor(
        layout: GroupLayout,
        size: Long? = null,
        arena: Arena = Arena.ofShared()
    ) : this(layout, arena, run {
        val defaultSize = layout.byteSize()
        val retainedSize = size ?: defaultSize

        check(retainedSize >= defaultSize) {
            "Allocation size must be greater than or equals to the struct size"
        }

        sqlite3_malloc64(retainedSize)
    })

    public actual override fun close() {
        arena?.close()?.also {
            sqlite3_free(pointer)
        }
    }
}