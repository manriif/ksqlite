package ksqlite.capi

import ksqlite.capi.types.Sqlite3ConfigSqlLogCallback
import ksqlite.capi.types.Sqlite3SqlLogEvent
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Dispatches [Sqlite3SqlLogEvent] to [callback].
 */
internal fun dispatchSqlLogEvent(
    callback: Sqlite3ConfigSqlLogCallback,
    userData: sqlite3_mutable_pointer?,
    type: Int,
    db: sqlite3,
    name: String?
) {
    callback(
        userData,
        db,
        when (type) {
            0 -> Sqlite3SqlLogEvent.DatabaseOpened(name!!)
            1 -> Sqlite3SqlLogEvent.StatementExecuted(name!!)
            2 -> Sqlite3SqlLogEvent.DatabaseClosed
            else -> error("Unknown sql log event type: $type")
        }
    )
}