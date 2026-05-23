package ksqlite.capi.memory

public actual open class StructPointer internal constructor(internal val pointer: Long) :
    StructPointerBase() {

    actual override val address: Long
        get() = pointer

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StructPointer) return false

        return pointer == other.pointer
    }

    override fun hashCode(): Int {
        return pointer.hashCode()
    }
}

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Whether this long represents a null pointer.
 */
internal val Long.isNullPointer: Boolean
    get() = this == 0L

/**
 * Returns [Pointer] instantiated after [factory] which is passed `this` non-null pointing [Long].
 */
internal fun <Pointer : StructPointer> Long.wrapOrNull(factory: (Long) -> Pointer): Pointer? {
    if (isNullPointer) {
        return null
    }

    return factory(this)
}