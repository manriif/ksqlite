package ksqlite.capi.memory

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.NativeFreeablePlacement
import kotlinx.cinterop.free
import kotlinx.cinterop.toLong

public actual open class Struct internal constructor(
    internal open val pointer: COpaquePointer,
    private val placement: NativeFreeablePlacement? = null
) : StructBase() {

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

    actual override fun free() {
        placement?.free(pointer)
    }
}