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
 * Alias to hte pointer type returned by JNI.
 */
internal typealias JniPointer = Long

/**
 * Whether this long represents a null pointer.
 */
internal val JniPointer.isNull: Boolean
    get() = this == 0L

/**
 * Returns `null` if `this` [Long] points to a null pointer.
 */
internal val JniPointer.orNull: JniPointer?
    get() = takeUnless { isNull }

/**
 * Returns `null` if `this` [Long] points to a null pointer.
 */
internal val JniPointer?.notNull: JniPointer
    get() = this ?: 0L

/**
 * Returns [Pointer] instantiated after [factory] which is passed `this` non-null pointing [Long].
 */
internal fun <Pointer : StructPointer> JniPointer.wrapOrNull(factory: (Long) -> Pointer): Pointer? {
    if (isNull) {
        return null
    }

    return factory(this)
}