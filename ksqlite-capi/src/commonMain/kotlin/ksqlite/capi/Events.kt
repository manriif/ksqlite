package ksqlite.capi

import ksqlite.capi.types.Sqlite3SqlLogCallback
import ksqlite.capi.types.Sqlite3SqlLogEvent
import ksqlite.capi.types.restricted
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Dispatches [Sqlite3SqlLogEvent] to [callback].
 */
internal fun dispatchSqlLogEvent(
    callback: Sqlite3SqlLogCallback,
    userData: sqlite3_mutable_pointer?,
    type: Int,
    name: () -> String,
    db: () -> sqlite3
) {
    callback(
        userData,
        restricted(db),
        when (type) {
            0 -> Sqlite3SqlLogEvent.DatabaseOpened(name())
            1 -> Sqlite3SqlLogEvent.StatementExecuted(name())
            2 -> Sqlite3SqlLogEvent.DatabaseClosed
            else -> error("Unknown sql log event type: $type")
        }
    )
}