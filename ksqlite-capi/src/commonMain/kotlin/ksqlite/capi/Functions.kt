/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("NOTHING_TO_INLINE")

package ksqlite.capi

import ksqlite.capi.callbacks.SqliteDestroyCallback
import ksqlite.capi.callbacks.SqliteFunctionFinalCallback
import ksqlite.capi.callbacks.SqliteFunctionFuncCallback
import ksqlite.capi.callbacks.SqliteFunctionInverseCallback
import ksqlite.capi.callbacks.SqliteFunctionStepCallback
import ksqlite.capi.callbacks.SqliteFunctionValueCallback
import ksqlite.capi.memory.ConcurrentMap

/**
 * Manages an application defined function from [sqlite3_create_function] and
 * [sqlite3_create_function_v2] and [sqlite3_create_window_function] callbacks.
 */
@PublishedApi
internal class ApplicationDefinedFunction<AppData>(
    val appData: AppData,
    private val destroy: SqliteDestroyCallback<in AppData>?,
    private val func: SqliteFunctionFuncCallback<in AppData>?,
    private val step: SqliteFunctionStepCallback<in AppData>?,
    private val final: SqliteFunctionFinalCallback<in AppData>?,
    private val value: SqliteFunctionValueCallback<in AppData>?,
    private val inverse: SqliteFunctionInverseCallback<in AppData>?
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

        destroy?.apply(appData)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Instances
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Ensures that [instance] is not null.
     */
    @IgnorableReturnValue
    private inline fun ensureInstanceExists(instance: Any?): Any = checkNotNull(instance) {
        "Aggregate context or auxiliary data was created but the Kotlin instance is lost..."
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
        aggregateContextInternal(context, false)
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
        val key = aggregateContextInternal(context, true) ?: return null // Out of memory

        return identifiedInstances.computeIfAbsent(key) {
            factory()
        }
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
        getAuxdataInternal(context, index)
    }

    /**
     * Sets the auxiliary data at [index].
     */
    fun <T : Any> setAuxiliaryData(
        context: sqlite3_context,
        index: Int,
        instance: T,
        destroy: SqliteDestroyCallback<T>?
    ) {
        var key: Long? = null

        val destroyAndRemove = SqliteDestroyCallback<Nothing?> {
            destroy?.apply(instance)
            key?.let(::uncacheInstance)
        }

        key = setAuxdataInternal(context, index, destroyAndRemove) ?: return // Out of memory
        identifiedInstances[key] = instance
    }

    ///////////////////////////////////////////////////////////////////////////
    // Callbacks
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Invokes the [func] callback.
     */
    fun callFunc(context: sqlite3_context, arguments: Array<sqlite3_value>) {
        func!!.apply(appData, context, arguments)
    }

    /**
     * Invokes the [step] callback.
     */
    fun callStep(context: sqlite3_context, arguments: Array<sqlite3_value>) {
        step!!.apply(appData, context, arguments)
    }

    /**
     * Invokes the [inverse] callback.
     */
    fun callInverse(context: sqlite3_context, arguments: Array<sqlite3_value>) {
        inverse!!.apply(appData, context, arguments)
    }

    /**
     * Invokes the [value] callback.
     */
    fun callValue(context: sqlite3_context) {
        value!!.apply(appData, context)
    }

    /**
     * Invokes the [final] callback and release the associated aggregate context Kotlin instance.
     */
    fun callFinal(context: sqlite3_context) {
        final!!.apply(appData, context)

        aggregateContextInternal(context, false)
            ?.let(::uncacheInstance)
    }
}

///////////////////////////////////////////////////////////////////////////
// Factory
///////////////////////////////////////////////////////////////////////////

/**
 * Invokes [block] with [function] and a destructor to use in place of application destroy.
 */
@Suppress("UNCHECKED_CAST")
private inline fun <AppData, R> appFunction(
    function: ApplicationDefinedFunction<AppData>,
    block: (ApplicationDefinedFunction<Any?>, SqliteDestroyCallback<Any?>) -> R
): R = block(function as ApplicationDefinedFunction<Any?>) { function.cleanup() }

/**
 * Invokes [block] with a [ApplicationDefinedFunction] for aggregate and scalar functions and a
 * destructor to use in place of application [destroy].
 *
 * TODO: throws if callbacks misuse ?
 */
internal inline fun <AppData, R> createFunction(
    appData: AppData,
    func: SqliteFunctionFuncCallback<in AppData>?,
    step: SqliteFunctionStepCallback<in AppData>?,
    final: SqliteFunctionFinalCallback<in AppData>?,
    destroy: SqliteDestroyCallback<in AppData>?,
    block: (
        fn: ApplicationDefinedFunction<Any?>?,
        funDestroy: SqliteDestroyCallback<Any?>?
    ) -> R
): R {
    if (func == null && step == null && final == null && destroy == null) {
        return block(null, null)
    }

    return appFunction(
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
}

/**
 * Returns a [ApplicationDefinedFunction] for window function and a destructor to use in place of
 * application [destroy].
 *
 * TODO: throws if callbacks misuse ?
 */
internal inline fun <AppData, R> createWindowFunction(
    appData: AppData,
    step: SqliteFunctionStepCallback<in AppData>?,
    final: SqliteFunctionFinalCallback<in AppData>?,
    value: SqliteFunctionValueCallback<in AppData>?,
    inverse: SqliteFunctionInverseCallback<in AppData>?,
    destroy: SqliteDestroyCallback<in AppData>?,
    block: (
        fn: ApplicationDefinedFunction<Any?>?,
        fnDestroy: SqliteDestroyCallback<Any?>?
    ) -> R
): R {
    if (step == null && final == null && value == null && inverse == null && destroy == null) {
        return block(null, null)
    }

    return appFunction(
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
}