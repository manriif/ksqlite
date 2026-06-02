package ksqlite.capi.memory

public actual open class Struct internal constructor(internal val pointer: Long) :
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

    actual override fun free() = Unit
}