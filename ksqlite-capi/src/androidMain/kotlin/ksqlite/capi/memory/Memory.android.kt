package ksqlite.capi.memory

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
internal fun <Pointer : Struct> JniPointer.wrapOrNull(factory: (Long) -> Pointer): Pointer? {
    if (isNull) {
        return null
    }

    return factory(this)
}