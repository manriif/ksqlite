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