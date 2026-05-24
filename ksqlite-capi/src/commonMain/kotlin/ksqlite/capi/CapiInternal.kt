package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value

///////////////////////////////////////////////////////////////////////////
// Native
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an identifier used as identifier for [context]. The same identifier must always be
 * returned for the same [context].
 *
 * If [create] is `false` and no identifier was created before or if [create] is `true` and creating
 * a new identifier fails then `null` must be returned.
 */
internal expect fun nativeAggregateContext(
    context: sqlite3_context,
    create: Boolean
): Long?

/**
 * Returns the identifier previously created with [nativeSetAuxdata] with the given parameters.
 */
internal expect fun nativeGetAuxdata(
    context: sqlite3_context,
    index: Int
): Long?

/**
 * Returns an identifier used as identifier for [context] and [index]. The same identifier must
 * always be returned for the same [context] and [index].
 *
 * If creating a new identifier fails then `null` must be returned.
 */
internal expect fun nativeSetAuxdata(
    context: sqlite3_context,
    index: Int,
    destroy: Sqlite3DestroyCallback<Nothing?>
): Long?

/**
 * Returns the [ApplicationDefinedFunction] instance from [context] user data.
 */
@PublishedApi
internal expect fun nativeUserData(context: sqlite3_context): ApplicationDefinedFunction<*>?

/**
 * Returns the pointer value
 */
@PublishedApi
internal expect fun nativeValuePointer(
    value: sqlite3_value,
    type: String?
): Any?

///////////////////////////////////////////////////////////////////////////
// Helpers
///////////////////////////////////////////////////////////////////////////

/**
 * Casts [instance] as [Data] or throws if [instance] is not an instance of [Data].
 */
@PublishedApi
internal inline fun <reified Data> castOrThrows(instance: Any?): Data? {
    if (instance == null) {
        return null
    }

    if (instance !is Data) {
        throw ClassCastException(
            "Expected type (${Data::class}) differs from actual type (${instance::class})"
        )
    }

    return instance
}