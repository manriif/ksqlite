package ksqlite.capi.memory

import ksqlite.foreign.structs.JniStruct

public actual open class Struct internal constructor(internal val pointer: JniPointer) :
    StructBase() {

    actual override val address: Long
        get() = pointer

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
    pointer: JniPointer,
    private val jniStruct: JniStruct?
) : Struct(pointer),
    AutoCloseable {

    internal constructor(jniStruct: JniStruct) : this(jniStruct.pointer, jniStruct)

    public actual override fun close() {
        jniStruct?.free()
    }
}