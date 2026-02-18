@file:Suppress("SpellCheckingInspection")

package ksqlite.types

import kotlin.reflect.KMutableProperty

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