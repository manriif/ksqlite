package ksqlite.capi

import ksqlite.capi.callbacks.Sqlite3CreateFunctionFinalCallback
import ksqlite.capi.callbacks.Sqlite3CreateFunctionFuncCallback
import ksqlite.capi.callbacks.Sqlite3CreateFunctionInverseCallback
import ksqlite.capi.callbacks.Sqlite3CreateFunctionStepCallback
import ksqlite.capi.callbacks.Sqlite3CreateFunctionValueCallback
import ksqlite.capi.utils.ConcurrentMap

/**
 * Holder for [sqlite3_create_function] and [sqlite3_create_function_v2] and
 * [sqlite3_create_window_function] callbacks.
 */
internal class CreateFunction<ClientData> private constructor(
    val func: Sqlite3CreateFunctionFuncCallback<ClientData>?,
    val step: Sqlite3CreateFunctionStepCallback<ClientData>?,
    val final: Sqlite3CreateFunctionFinalCallback<ClientData>?,
    val value: Sqlite3CreateFunctionValueCallback<ClientData>?,
    val inverse: Sqlite3CreateFunctionInverseCallback<ClientData>?,
) {

    // TODO
    private val aggregateContexts = ConcurrentMap<Long, Any>()

    /**
     * Regular function.
     */
    constructor(
        func: Sqlite3CreateFunctionFuncCallback<ClientData>?,
        step: Sqlite3CreateFunctionStepCallback<ClientData>?,
        final: Sqlite3CreateFunctionFinalCallback<ClientData>?
    ) : this(
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
        step: Sqlite3CreateFunctionStepCallback<ClientData>?,
        final: Sqlite3CreateFunctionFinalCallback<ClientData>?,
        value: Sqlite3CreateFunctionValueCallback<ClientData>?,
        inverse: Sqlite3CreateFunctionInverseCallback<ClientData>?,
    ) : this(
        func = null,
        step = step,
        final = final,
        value = value,
        inverse = inverse
    )
}