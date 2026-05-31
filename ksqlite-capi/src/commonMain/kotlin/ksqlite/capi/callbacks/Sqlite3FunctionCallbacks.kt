@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.callbacks

import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_value

/**
 * Callback with one parameter to use with [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3Function1Callback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/create_function.html).
     */
    public fun handle(
        appData: AppData,
        context: sqlite3_context
    )
}

/**
 * Callback with two parameters of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3Function3Callback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/create_function.html).
     */
    public fun handle(
        appData: AppData,
        context: sqlite3_context,
        arguments: Array<sqlite3_value>
    )
}

/**
 * Callback to use with the `func` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3FunctionFuncCallback<AppData> : Sqlite3Function3Callback<AppData>

/**
 * Callback to use with the `step` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3FunctionStepCallback<AppData> : Sqlite3Function3Callback<AppData>

/**
 * Callback to use with the `inverse` parameter of [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3FunctionInverseCallback<AppData> : Sqlite3Function3Callback<AppData>

/**
 * Callback to use with the `final` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3FunctionFinalCallback<AppData> : Sqlite3Function1Callback<AppData>

/**
 * Callback to use with the `value` parameter of [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface Sqlite3FunctionValueCallback<AppData> : Sqlite3Function1Callback<AppData>

