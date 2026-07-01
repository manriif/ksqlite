package ksqlite.capi

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.memory.Buffer
import ksqlite.capi.memory.Int64OutputParam
import ksqlite.types.SqliteSerializeFlag

///////////////////////////////////////////////////////////////////////////
// Buffer
///////////////////////////////////////////////////////////////////////////

/**
 * Returns the value blob content as a [Buffer].
 */
internal expect fun columnBufferInternal(
    stmt: sqlite3_stmt,
    index: Int
): Buffer?

/**
 * Returns the value blob content as a [Buffer].
 */
internal expect fun valueBufferInternal(value: sqlite3_value): Buffer?

///////////////////////////////////////////////////////////////////////////
// Non-canonical behaviour
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an identifier used as identifier for [context]. The same identifier must always be
 * returned for the same [context].
 *
 * If [create] is `false` and no identifier was created before or if [create] is `true` and creating
 * a new identifier fails then `null` must be returned.
 */
internal expect fun aggregateContextInternal(
    context: sqlite3_context,
    create: Boolean
): Long?

/**
 * Returns the identifier previously created with [setAuxdataInternal] with the given parameters.
 */
internal expect fun getAuxdataInternal(
    context: sqlite3_context,
    index: Int
): Long?

/**
 * Returns an identifier used as identifier for [context] and [index]. The same identifier must
 * always be returned for the same [context] and [index].
 *
 * If creating a new identifier fails then `null` must be returned.
 */
internal expect fun setAuxdataInternal(
    context: sqlite3_context,
    index: Int,
    destroy: SqliteDestroyCallback<Nothing?>
): Long?

/**
 * Returns the [ApplicationDefinedFunction] instance from [context] user data.
 */
@PublishedApi
internal expect fun userDataInternal(context: sqlite3_context): ApplicationDefinedFunction<*>?

/**
 * Returns a buffer to the serialized database.
 */
internal expect fun serializeInternal(
    db: sqlite3,
    database: String?,
    outSize: Int64OutputParam,
    flags: SqliteSerializeFlag?
): Buffer?

/**
 * Returns the pointer value.
 */
@PublishedApi
internal expect fun valuePointerInternal(
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