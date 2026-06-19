@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.types.SqliteTraceEventCode

/**
 * Event passed as parameter to [SqliteTraceCallback].
 */
public sealed interface SqliteTraceEvent {

    /**
     * [SqliteTraceEventCode.STMT] related event.
     */
    public class Stmt(
        public val stmt: sqlite3_stmt,
        public val sql: String
    ) : SqliteTraceEvent

    /**
     * [SqliteTraceEventCode.PROFILE] related event.
     */
    public class Profile(
        public val stmt: sqlite3_stmt,
        public val nanos: Long
    ) : SqliteTraceEvent

    /**
     * [SqliteTraceEventCode.ROW] related event.
     */
    public class Row(public val stmt: sqlite3_stmt) : SqliteTraceEvent

    /**
     * [SqliteTraceEventCode.CLOSE] related event.
     */
    public class Close(public val db: sqlite3) : SqliteTraceEvent
}