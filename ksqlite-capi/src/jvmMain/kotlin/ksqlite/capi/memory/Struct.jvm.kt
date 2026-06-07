package ksqlite.capi.memory

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

public actual open class Struct internal constructor(
    internal val pointer: MemorySegment,
    private val arena: Arena? = null
) : StructBase() {

    internal constructor(
        arena: Arena = Arena.ofShared(),
        allocate: Arena.() -> MemorySegment
    ) : this(arena.allocate(), arena)

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

    actual override fun free() {
        arena?.close()
    }
}