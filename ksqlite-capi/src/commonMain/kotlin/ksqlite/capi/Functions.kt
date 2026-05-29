@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3FunctionFinalCallback
import ksqlite.capi.callbacks.Sqlite3FunctionFuncCallback
import ksqlite.capi.callbacks.Sqlite3FunctionInverseCallback
import ksqlite.capi.callbacks.Sqlite3FunctionStepCallback
import ksqlite.capi.callbacks.Sqlite3FunctionValueCallback
import ksqlite.capi.callbacks.Sqlite3DestroyCallback
import ksqlite.capi.memory.ConcurrentMap
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value

/**
 * Manages an application defined function from [sqlite3_create_function] and
 * [sqlite3_create_function_v2] and [sqlite3_create_window_function] callbacks.
 */
@PublishedApi
internal class ApplicationDefinedFunction<AppData>(
    val appData: AppData,
    private val destroy: Sqlite3DestroyCallback<AppData>?,
    private val func: Sqlite3FunctionFuncCallback<AppData>?,
    private val step: Sqlite3FunctionStepCallback<AppData>?,
    private val final: Sqlite3FunctionFinalCallback<AppData>?,
    private val value: Sqlite3FunctionValueCallback<AppData>?,
    private val inverse: Sqlite3FunctionInverseCallback<AppData>?
) {

    /**
     * Holds aggregate contexts + auxdata instances.
     * Keys are platform specific identifiers.
     */
    private val identifiedInstances = ConcurrentMap<Long, Any>()

    ///////////////////////////////////////////////////////////////////////////
    // Cleanup
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Invokes client destroy and ensures all identified data have been cleaned up correctly.
     */
    fun cleanup() {
        check(identifiedInstances.isEmpty()) {
            "Not all of the identified instances were destroyed"
        }

        destroy?.handle(appData)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Instances
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Ensures that [instance] is not null.
     */
    @IgnorableReturnValue
    private inline fun ensureInstanceExists(instance: Any?): Any {
        return checkNotNull(instance) {
            "Aggregate context or auxiliary data was created but the Kotlin instance is lost"
        }
    }

    /**
     * Returns the cached instance for the key supplied by [getKey] or `null` if the supplied key is
     * `null`.
     */
    private inline fun getCachedInstance(getKey: () -> Long?): Any? {
        val key = getKey() ?: return null // No data exist or old is destroyed
        return ensureInstanceExists(identifiedInstances[key])
    }

    /**
     * Caches the [instance] ensuring no one already exists with [key].
     */
    @IgnorableReturnValue
    private fun cacheInstance(key: Long, instance: Any): Any {
        check(identifiedInstances.put(key, instance) == null) {
            "An instance of Aggregate context or auxiliary data already exists for the supplied " +
                    "key, memory leak is near"
        }

        return instance
    }

    /**
     * Removes the instances associated with [key] ensuring it exists.
     */
    private fun uncacheInstance(key: Long) {
        ensureInstanceExists(identifiedInstances.remove(key))
    }

    ///////////////////////////////////////////////////////////////////////////
    // Aggregate context
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the aggregate context associated with [context] or `null` if no aggregate context
     * was created yet.
     */
    fun getAggregateContextOrNull(context: sqlite3_context): Any? = getCachedInstance {
        nativeAggregateContext(context, false)
    }

    /**
     * Returns the aggregate context associated with [context] or create a new one using
     * [factory] and returns it.
     *
     * If the system runs out of memory, then `null` is returned.
     */
    fun getOrCreateAggregateContext(
        context: sqlite3_context,
        factory: () -> Any
    ): Any? {
        val key = nativeAggregateContext(context, true) ?: return null // Out of memory
        return cacheInstance(key, factory())
    }

    ///////////////////////////////////////////////////////////////////////////
    // Auxiliary data
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the auxiliary data for [index] or `null` if no auxiliary data exists.
     */
    fun getAuxiliaryDataOrNull(
        context: sqlite3_context,
        index: Int
    ): Any? = getCachedInstance {
        nativeGetAuxdata(context, index)
    }

    /**
     * Sets the auxiliary data at [index].
     */
    fun <T : Any> setAuxiliaryData(
        context: sqlite3_context,
        index: Int,
        instance: T,
        destroy: Sqlite3DestroyCallback<T>?
    ) {
        var key: Long? = null

        val destroyAndRemove = Sqlite3DestroyCallback<Nothing?> {
            destroy?.handle(instance)
            key?.let(::uncacheInstance)
        }

        key = nativeSetAuxdata(context, index, destroyAndRemove) ?: return // Out of memory
        cacheInstance(key, instance)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Invokes the [func] callback.
     */
    fun callFunc(context: sqlite3_context, values: Array<sqlite3_value>) {
        func!!.handle(appData, context, values)
    }

    /**
     * Invokes the [step] callback.
     */
    fun callStep(context: sqlite3_context, values: Array<sqlite3_value>) {
        step!!.handle(appData, context, values)
    }

    /**
     * Invokes the [inverse] callback.
     */
    fun callInverse(context: sqlite3_context, values: Array<sqlite3_value>) {
        inverse!!.handle(appData, context, values)
    }

    /**
     * Invokes the [value] callback.
     */
    fun callValue(context: sqlite3_context) {
        value!!.handle(appData, context)
    }

    /**
     * Invokes the [final] callback and release the associated aggregate context Kotlin instance.
     */
    fun callFinal(context: sqlite3_context) {
        final!!.handle(appData, context)

        nativeAggregateContext(context, false)
            ?.let(::uncacheInstance)
    }
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes [block] with [function] and a destroy to use in place of client destroy.
 */
@Suppress("UNCHECKED_CAST")
private inline fun <AppData, R> appFunction(
    function: ApplicationDefinedFunction<AppData>,
    block: (ApplicationDefinedFunction<Any?>, Sqlite3DestroyCallback<Any?>) -> R
): R = block(function as ApplicationDefinedFunction<Any?>) { function.cleanup() }

/**
 * Invokes [block] with a [ApplicationDefinedFunction] for aggregate and scalar functions and a destroy
 * to use in place of client [destroy].
 */
internal inline fun <AppData, R> appFunction(
    appData: AppData,
    func: Sqlite3FunctionFuncCallback<AppData>?,
    step: Sqlite3FunctionStepCallback<AppData>?,
    final: Sqlite3FunctionFinalCallback<AppData>?,
    destroy: Sqlite3DestroyCallback<AppData>?,
    block: (
        fn: ApplicationDefinedFunction<Any?>,
        funDestroy: Sqlite3DestroyCallback<Any?>
    ) -> R
): R = appFunction(
    function = ApplicationDefinedFunction(
        appData = appData,
        destroy = destroy,
        func = func,
        step = step,
        final = final,
        value = null,
        inverse = null
    ),
    block = block
)

/**
 * Returns a [ApplicationDefinedFunction] for window function and a destroy to use in place of client
 * [destroy].
 */
internal inline fun <AppData, R> appWindowFunction(
    appData: AppData,
    step: Sqlite3FunctionStepCallback<AppData>?,
    final: Sqlite3FunctionFinalCallback<AppData>?,
    value: Sqlite3FunctionValueCallback<AppData>?,
    inverse: Sqlite3FunctionInverseCallback<AppData>?,
    destroy: Sqlite3DestroyCallback<AppData>?,
    block: (
        fn: ApplicationDefinedFunction<Any?>,
        fnDestroy: Sqlite3DestroyCallback<Any?>
    ) -> R
): R = appFunction(
    function = ApplicationDefinedFunction(
        appData = appData,
        destroy = destroy,
        func = null,
        step = step,
        final = final,
        value = value,
        inverse = inverse
    ),
    block = block
)