@file:Suppress("SpellCheckingInspection")

package ksqlite.types

import kotlin.reflect.KMutableProperty

/**
 * Generic callback invoked when a no longer necessary object is being detroyed by sqlite.
 */
public typealias Sqlite3DestructorCallback = () -> Unit

/**
 * Callback for [ksqlite.sqlite3_auto_extension]
 */
public typealias Sqlite3AutoExtensionCallback = (
    db: sqlite3,
    errorMsg: KMutableProperty<String>,
    routines: sqlite3_api_routines
) -> Int

/**
 * Callback for [ksqlite.sqlite3_busy_handler].
 */
public typealias Sqlite3BusyHandlerCallback = (count: Int) -> Int

/**
 * Callback for [ksqlite.sqlite3_collation_needed].
 */
public typealias Sqlite3CollationNeededCallback = (
    db: sqlite3,
    encoding: Sqlite3TextEncoding.Set2,
    name: String
) -> Int

/**
 * Callback for [ksqlite.sqlite3_create_collation] and [ksqlite.sqlite3_create_collation_v2].
 */
public typealias Sqlite3CollationCompareCallback = (
    left: String,
    right: String
) -> Int

/**
 * Callback for [ksqlite.sqlite3_commit_hook].
 */
public typealias Sqlite3CommitHookCallback = () -> Int

/**
 * Callback with two parameters of [ksqlite.sqlite3_create_function],
 * [ksqlite.sqlite3_create_function_v2] and [ksqlite.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunction2Callback = (
    context: sqlite3_context,
    values: Array<sqlite3_value>
) -> Unit

/**
 * Callback with one parameter of [ksqlite.sqlite3_create_function],
 * [ksqlite.sqlite3_create_function_v2] and [ksqlite.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunction1Callback = (context: sqlite3_context) -> Unit

/**
 * Callback for the `func` parameter of [ksqlite.sqlite3_create_function],
 * [ksqlite.sqlite3_create_function_v2] and [ksqlite.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionFuncCallback = Sqlite3CreateFunction2Callback

/**
 * Callback for the `step` parameter of [ksqlite.sqlite3_create_function],
 * [ksqlite.sqlite3_create_function_v2] and [ksqlite.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionStepCallback = Sqlite3CreateFunction2Callback

/**
 * Callback for the `inverse` parameter of [ksqlite.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionInverseCallback = Sqlite3CreateFunction2Callback

/**
 * Callback for the `final` parameter of [ksqlite.sqlite3_create_function],
 * [ksqlite.sqlite3_create_function_v2] and [ksqlite.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionFinalCallback = Sqlite3CreateFunction1Callback

/**
 * Callback for the `value` parameter of [ksqlite.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionValueCallback = Sqlite3CreateFunction1Callback

/**
 * Callback for [ksqlite.sqlite3_exec].
 */
public typealias Sqlite3ExecCallback = (
    columnCount: Int,
    columnValues: Array<String?>,
    columnNames: Array<String>
) -> Int

/**
 * Callback for [Sqlite3ConfigOption.LOG].
 */
public typealias Sqlite3LogCallback = (
    errorCode: Int,
    errorMsg: String
) -> Unit

/**
 * Callback for [Sqlite3ConfigOption.SQLLOG].
 */
public typealias Sqlite3SqlLogCallback = (
    db: sqlite3,
    event: Sqlite3SqlLogEvent
) -> Unit