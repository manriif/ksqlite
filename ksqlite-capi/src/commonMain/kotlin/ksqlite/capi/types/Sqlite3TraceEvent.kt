@file:Suppress("ClassName")

package ksqlite.capi.types

/**
 * Event passed as parameter to [ksqlite.capi.callbacks.Sqlite3TraceCallback].
 */
public sealed interface Sqlite3TraceEvent {

    /**
     * [Sqlite3TraceCode.STMT] related event.
     */
    public class Stmt(
        public val stmt: sqlite3_stmt,
        public val sql: String
    ) : Sqlite3TraceEvent

    /**
     * [Sqlite3TraceCode.PROFILE] related event.
     */
    public class Profile(
        public val stmt: sqlite3_stmt,
        public val nanos: Long
    ) : Sqlite3TraceEvent

    /**
     * [Sqlite3TraceCode.ROW] related event.
     */
    public class Row(public val stmt: sqlite3_stmt) : Sqlite3TraceEvent

    /**
     * [Sqlite3TraceCode.CLOSE] related event.
     */
    public class Close(public val db: sqlite3) : Sqlite3TraceEvent
}