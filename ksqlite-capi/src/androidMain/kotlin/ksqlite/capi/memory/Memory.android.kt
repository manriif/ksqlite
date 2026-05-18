package ksqlite.capi.memory

public actual open class GenericPointer internal constructor(internal val pointer: Long)

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
internal fun <Pointer: GenericPointer> Long.wrapOrNull(factory: (Long) -> Pointer): Pointer? {
    if (isNullPointer) {
        return null
    }

    return factory(this)
}