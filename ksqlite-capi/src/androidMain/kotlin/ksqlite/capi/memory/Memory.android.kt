package ksqlite.capi.memory

import ksqlite.nativeReadString

///////////////////////////////////////////////////////////////////////////
// Pointer
///////////////////////////////////////////////////////////////////////////

/**
 * Alias to hte pointer type returned by JNI.
 */
internal typealias JniPointer = Long

/**
 * Alias to hte pointer type returned by JNI.
 */
internal val NullPtr: JniPointer
    inline get() = 0L

/**
 * Whether this long represents a null pointer.
 */
internal val JniPointer.isNull: Boolean
    inline get() = this == NullPtr

/**
 * Returns `null` if `this` [Long] points to a null pointer.
 */
internal val JniPointer.orNull: JniPointer?
    inline get() = takeUnless { isNull }

/**
 * Returns `null` if `this` [Long] points to a null pointer.
 */
internal val JniPointer?.notNull: JniPointer
    inline get() = this ?: NullPtr

/**
 * Returns [Pointer] instantiated after [factory] which is passed `this` non-null pointing [Long].
 */
internal fun <Pointer : Struct> JniPointer.wrapOrNull(factory: (Long) -> Pointer): Pointer? =
    orNull?.let(factory)

///////////////////////////////////////////////////////////////////////////
// String
///////////////////////////////////////////////////////////////////////////

/**
 * Reads bytes until null termination marker is found and returns the bytes read as [String].
 * If `this` pointer points to `null` then `null` is returned.
 */
internal fun JniPointer.toKStringFromUtf8OrNull(): String? = nativeReadString(orNull ?: return null)