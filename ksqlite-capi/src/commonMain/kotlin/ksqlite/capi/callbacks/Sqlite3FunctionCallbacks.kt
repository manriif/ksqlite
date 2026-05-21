@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.callbacks

import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value

/**
 * Callback with one parameter to use with [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3CreateFunction1Callback<ClientData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/create_function.html).
     */
    public fun handle(
        clientData: ClientData,
        context: sqlite3_context
    )
}

/**
 * Callback with two parameters of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3CreateFunction3Callback<ClientData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/create_function.html).
     */
    public fun handle(
        clientData: ClientData,
        context: sqlite3_context,
        values: Array<sqlite3_value>
    )
}

/**
 * Callback to use with the `func` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionFuncCallback<ClientData> =
        Sqlite3CreateFunction3Callback<ClientData>

/**
 * Callback to use with the `step` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionStepCallback<ClientData> =
        Sqlite3CreateFunction3Callback<ClientData>

/**
 * Callback to use with the `inverse` parameter of [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionInverseCallback<ClientData> =
        Sqlite3CreateFunction3Callback<ClientData>

/**
 * Callback to use with the `final` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionFinalCallback<ClientData> =
        Sqlite3CreateFunction1Callback<ClientData>

/**
 * Callback to use with the `value` parameter of [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionValueCallback<ClientData> =
        Sqlite3CreateFunction1Callback<ClientData>

