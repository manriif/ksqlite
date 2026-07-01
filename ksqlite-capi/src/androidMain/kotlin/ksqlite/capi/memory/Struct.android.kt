package ksqlite.capi.memory

import ksqlite.foreign.structs.JniStruct

public actual open class Struct internal constructor(
    internal val pointer: JniPointer,
    protected open val jniStruct: JniStruct? = null
) : StructBase() {

    internal constructor(jniStruct: JniStruct) : this(jniStruct.pointer, jniStruct)

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

    actual override fun free() {
        jniStruct?.free()
    }
}