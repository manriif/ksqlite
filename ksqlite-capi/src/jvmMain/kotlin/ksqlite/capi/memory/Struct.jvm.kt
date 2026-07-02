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

public open class ReinterpretedStruct internal constructor(
    protected val arena: Arena,
    pointer: MemorySegment,
) : Struct(pointer),
    AutoCloseable {

    internal constructor(
        pointer: MemorySegment,
        layout: GroupLayout,
        arena: Arena = Arena.ofConfined()
    ) : this(arena, pointer.reinterpret(layout.byteSize(), arena, null))

    public override fun close() {
        arena.close()
    }
}

public actual open class AllocatedStruct internal constructor(
    private val allocatedPointer: MemorySegment,
    layout: GroupLayout,
    arena: Arena
) : ReinterpretedStruct(allocatedPointer, layout, arena) {

    internal constructor(
        layout: GroupLayout,
        size: Long? = null,
        arena: Arena = Arena.ofShared()
    ) : this(sqlite3_malloc64(checkStructSize(layout.byteSize(), size)), layout, arena)

    public actual override fun close() {
        super.close()
        sqlite3_free(allocatedPointer)
    }
}