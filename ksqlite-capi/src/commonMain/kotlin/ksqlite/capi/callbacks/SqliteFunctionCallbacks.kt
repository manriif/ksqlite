@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.callbacks

import ksqlite.capi.sqlite3_context
import ksqlite.capi.sqlite3_value

/**
 * Callback with one parameter to use with [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface SqliteFunction1Callback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/create_function.html).
     */
    public fun apply(
        appData: AppData,
        context: sqlite3_context
    )
}

/**
 * Callback with two parameters of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface SqliteFunction3Callback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/create_function.html).
     */
    public fun apply(
        appData: AppData,
        context: sqlite3_context,
        arguments: Array<sqlite3_value>
    )
}

/**
 * Callback to use with the `func` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface SqliteFunctionFuncCallback<AppData> : SqliteFunction3Callback<AppData>

/**
 * Callback to use with the `step` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface SqliteFunctionStepCallback<AppData> : SqliteFunction3Callback<AppData>

/**
 * Callback to use with the `inverse` parameter of [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface SqliteFunctionInverseCallback<AppData> : SqliteFunction3Callback<AppData>

/**
 * Callback to use with the `final` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface SqliteFunctionFinalCallback<AppData> : SqliteFunction1Callback<AppData>

/**
 * Callback to use with the `value` parameter of [ksqlite.capi.sqlite3_create_window_function].
 */
public fun interface SqliteFunctionValueCallback<AppData> : SqliteFunction1Callback<AppData>

