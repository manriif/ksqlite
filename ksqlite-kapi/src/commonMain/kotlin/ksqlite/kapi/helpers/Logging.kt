package ksqlite.kapi.helpers

import ksqlite.capi.sqlite3_log
import ksqlite.types.SqliteResultCode

/**
 * Logs [message] using SQLite logging API.
 * SQLite must have been initialized.
 */
internal fun ksqliteLog(
    message: String,
    resultCode: SqliteResultCode.Failure = SqliteResultCode.ERROR
) {
    sqlite3_log(resultCode.code, message)
}

/**
 * Logs [throwable] (unexpected) using SQLite logging API.
 * SQLite must have been initialized.
 */
internal fun ksqliteLog(
    throwable: Throwable,
    resultCode: SqliteResultCode.Failure = SqliteResultCode.MISUSE
) = ksqliteLog(
    message = "Uncaught unexpected exception\n${throwable.stackTraceToString()}",
    resultCode = resultCode
)