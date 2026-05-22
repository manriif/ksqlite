@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi

import ksqlite.capi.memory.ConcurrentMap
import ksqlite.capi.callbacks.Sqlite3CreateFunctionFinalCallback
import ksqlite.capi.callbacks.Sqlite3CreateFunctionFuncCallback
import ksqlite.capi.callbacks.Sqlite3CreateFunctionInverseCallback
import ksqlite.capi.callbacks.Sqlite3CreateFunctionStepCallback
import ksqlite.capi.callbacks.Sqlite3CreateFunctionValueCallback
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value
import kotlin.reflect.KClass

///////////////////////////////////////////////////////////////////////////
// Native
///////////////////////////////////////////////////////////////////////////

/**
 * Returns an identifier used as identifier for [context]. The same identifier must always be
 * returned for the same [context].
 *
 * If [create] is `false` and no identifier was created before or if [create] is `true` and creating
 * a new identifier fails then `0` must be returned.
 */
internal expect fun nativeAggregateContext(
    context: sqlite3_context,
    create: Boolean
): Long

/**
 * Returns the [Udf] instance from [context] user data.
 */
@PublishedApi
internal expect fun nativeUserData(context: sqlite3_context): Udf<*>

///////////////////////////////////////////////////////////////////////////
// User defined function instance
///////////////////////////////////////////////////////////////////////////

/**
 * Manages a user defined function from [sqlite3_create_function] and [sqlite3_create_function_v2]
 * and [sqlite3_create_window_function] callbacks.
 */
@PublishedApi
internal class Udf<ClientData> private constructor(
    val clientData: ClientData,
    private val func: Sqlite3CreateFunctionFuncCallback<ClientData>?,
    private val step: Sqlite3CreateFunctionStepCallback<ClientData>?,
    private val final: Sqlite3CreateFunctionFinalCallback<ClientData>?,
    private val value: Sqlite3CreateFunctionValueCallback<ClientData>?,
    private val inverse: Sqlite3CreateFunctionInverseCallback<ClientData>?
) {

    private val aggregateContexts = ConcurrentMap<Long, Any>()

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Regular function.
     */
    constructor(
        clientData: ClientData,
        func: Sqlite3CreateFunctionFuncCallback<ClientData>?,
        step: Sqlite3CreateFunctionStepCallback<ClientData>?,
        final: Sqlite3CreateFunctionFinalCallback<ClientData>?
    ) : this(
        clientData = clientData,
        func = func,
        step = step,
        final = final,
        value = null,
        inverse = null
    )

    /**
     * Window function.
     */
    constructor(
        clientData: ClientData,
        step: Sqlite3CreateFunctionStepCallback<ClientData>?,
        final: Sqlite3CreateFunctionFinalCallback<ClientData>?,
        value: Sqlite3CreateFunctionValueCallback<ClientData>?,
        inverse: Sqlite3CreateFunctionInverseCallback<ClientData>?,
    ) : this(
        clientData = clientData,
        func = null,
        step = step,
        final = final,
        value = value,
        inverse = inverse
    )

    ///////////////////////////////////////////////////////////////////////////
    // Aggregate
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Ensures that [instance] is not null.
     */
    @IgnorableReturnValue
    private inline fun ensureAggregateContextExists(instance: Any?): Any {
        return checkNotNull(instance) {
            "Aggregate context was created but the Kotlin instance was not found in the map"
        }
    }

    /**
     * Ensures that [value] is of type [T].
     */
    private fun <T : Any> castAggregateContext(klass: KClass<T>, value: Any): T {
        if (!klass.isInstance(value)) {
            throw ClassCastException(
                "Aggregate context of type ${value::class} is not an instance of expected class " +
                        "($klass)"
            )
        }

        @Suppress("UNCHECKED_CAST")
        return value as T
    }

    /**
     * Returns the aggregate context [T] associated with [context] or `null` if no aggregate context
     * was created yet.
     */
    fun <T : Any> getOrNull(
        context: sqlite3_context,
        klass: KClass<T>
    ): T? {
        val key = nativeAggregateContext(context, false)

        if (key == 0L) {
            return null // No context exist yet
        }

        return castAggregateContext(
            klass = klass,
            value = ensureAggregateContextExists(aggregateContexts[key])
        )
    }

    /**
     * Returns the aggregate context [T] associated with [context] or create a new one using
     * [factory] and returns it.
     *
     * If the system runs out of memory, then `null` is returned.
     */
    fun <T : Any> getOrCreate(
        context: sqlite3_context,
        klass: KClass<T>,
        factory: () -> T
    ): T? {
        val key = nativeAggregateContext(context, true)

        if (key == 0L) {
            return null // Out of memory
        }

        return castAggregateContext(
            klass = klass,
            value = aggregateContexts.computeIfAbsent(key) { factory() }
        )
    }

    ///////////////////////////////////////////////////////////////////////////
    // Call
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Invokes the [func] callback.
     */
    fun callFunc(context: sqlite3_context, values: Array<sqlite3_value>) {
        func!!.handle(clientData, context, values)
    }

    /**
     * Invokes the [step] callback.
     */
    fun callStep(context: sqlite3_context, values: Array<sqlite3_value>) {
        step!!.handle(clientData, context, values)
    }

    /**
     * Invokes the [inverse] callback.
     */
    fun callInverse(context: sqlite3_context, values: Array<sqlite3_value>) {
        inverse!!.handle(clientData, context, values)
    }

    /**
     * Invokes the [value] callback.
     */
    fun callValue(context: sqlite3_context) {
        value!!.handle(clientData, context)
    }

    /**
     * Invokes the [final] callback and release the associated aggregate context Kotlin instance.
     */
    fun callFinal(context: sqlite3_context) {
        final!!.handle(clientData, context)

        val key = nativeAggregateContext(context, false)

        if (key != 0L) {
            ensureAggregateContextExists(aggregateContexts.remove(key))
        }
    }
}