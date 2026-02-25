@file:Suppress("SpellCheckingInspection")

package ksqlite.capi.types

/**
 * Generic callback invoked when a no longer necessary object is being detroyed by sqlite.
 */
public typealias Sqlite3DestructorCallback = (userData: sqlite3_mutable_pointer?) -> Unit

/**
 * Callback for [ksqlite.capi.sqlite3_auto_extension].
 */
public typealias Sqlite3AutoExtensionCallback = (
    db: Sqlite3Param<sqlite3>,
    routines: Sqlite3Param<sqlite3_api_routines>,
    errorMsg: (String) -> Unit
) -> Sqlite3Result

/**
 * Callback for [ksqlite.capi.sqlite3_busy_handler].
 */
public typealias Sqlite3BusyHandlerCallback = (
    userData: sqlite3_mutable_pointer?,
    count: Int
) -> Int

/**
 * Callback for [ksqlite.capi.sqlite3_collation_needed].
 */
public typealias Sqlite3CollationNeededCallback = (
    userData: sqlite3_mutable_pointer?,
    db: Sqlite3Param<sqlite3>,
    encoding: Sqlite3TextEncoding.Set2,
    name: String
) -> Unit

/**
 * Callback for [ksqlite.capi.sqlite3_create_collation] and [ksqlite.capi.sqlite3_create_collation_v2].
 */
public typealias Sqlite3CollationCompareCallback = (
    userData: sqlite3_mutable_pointer?,
    left: String,
    right: String
) -> Int

/**
 * Callback for [ksqlite.capi.sqlite3_commit_hook].
 */
public typealias Sqlite3CommitCallback = (userData: sqlite3_mutable_pointer?) -> Int

/**
 * Callback with two parameters of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunction2Callback = (
    context: Sqlite3Param<sqlite3_context>,
    values: Array<sqlite3_value>
) -> Unit

/**
 * Callback with one parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunction1Callback = (context: Sqlite3Param<sqlite3_context>) -> Unit

/**
 * Callback for the `func` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionFuncCallback = Sqlite3CreateFunction2Callback

/**
 * Callback for the `step` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionStepCallback = Sqlite3CreateFunction2Callback

/**
 * Callback for the `inverse` parameter of [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionInverseCallback = Sqlite3CreateFunction2Callback

/**
 * Callback for the `final` parameter of [ksqlite.capi.sqlite3_create_function],
 * [ksqlite.capi.sqlite3_create_function_v2] and [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionFinalCallback = Sqlite3CreateFunction1Callback

/**
 * Callback for the `value` parameter of [ksqlite.capi.sqlite3_create_window_function].
 */
public typealias Sqlite3CreateFunctionValueCallback = Sqlite3CreateFunction1Callback

/**
 * Callback for [ksqlite.capi.sqlite3_exec].
 */
public typealias Sqlite3ExecCallback = (
    userData: sqlite3_mutable_pointer?,
    columnCount: Int,
    columnValues: Array<String?>,
    columnNames: Array<String>
) -> Int

/**
 * Callback for [Sqlite3ConfigOption.LOG].
 */
public typealias Sqlite3LogCallback = (
    userData: sqlite3_mutable_pointer?,
    errorCode: Int,
    errorMsg: String
) -> Unit

/**
 * Callback for [Sqlite3ConfigOption.SQLLOG].
 */
public typealias Sqlite3SqlLogCallback = (
    userData: sqlite3_mutable_pointer?,
    db: Sqlite3Param<sqlite3>,
    event: Sqlite3SqlLogEvent
) -> Unit

/**
 * Callback for [ksqlite.capi.sqlite3_preupdate_hook].
 */
public typealias Sqlite3PreUpdateCallback = (
    userData: sqlite3_mutable_pointer?,
    db: Sqlite3Param<sqlite3>,
    action: Sqlite3ActionCode.Dml,
    dbName: String,
    tableName: String,
    oldRowId: Long,
    newRowId: Long
) -> Unit

/**
 * Callback for [ksqlite.capi.sqlite3_progress_handler].
 */
public typealias Sqlite3ProgressCallback = (userData: sqlite3_mutable_pointer?) -> Int

/**
 * Callback for [ksqlite.capi.sqlite3_rollback_hook].
 */
public typealias Sqlite3RollbackCallback = (userData: sqlite3_mutable_pointer?) -> Unit

/**
 * Callback for [ksqlite.capi.sqlite3_set_authorizer].
 */
public typealias Sqlite3SetAuthorizerCallback = (
    userData: sqlite3_mutable_pointer?,
    action: Sqlite3ActionCode,
    param3: String?,
    param4: String?,
    param5: String?,
    param6: String?
) -> Sqlite3AuthorizerCode

/**
 * Callback for [ksqlite.capi.sqlite3_trace_v2].
 */
public typealias Sqlite3TraceCallback = (
    userData: sqlite3_mutable_pointer?,
    event: Sqlite3TraceEvent
) -> Int

/**
 * Callback for [ksqlite.capi.sqlite3_update_hook].
 */
public typealias Sqlite3UpdateCallback = (
    userData: sqlite3_mutable_pointer?,
    action: Sqlite3ActionCode.Dml,
    dbName: String,
    tableName: String,
    rowId: Long
) -> Unit
