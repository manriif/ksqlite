@file:Suppress("ClassName")

package ksqlite.capi.types

/**
 * Event passed as parameter to [ksqlite.capi.callbacks.SqliteTraceCallback].
 */
public sealed interface SqliteTraceEvent {

    /**
     * [ksqlite.types.SqliteTraceCode.STMT] related event.
     */
    public class Stmt(
        public val stmt: sqlite3_stmt,
        public val sql: String
    ) : SqliteTraceEvent

    /**
     * [ksqlite.types.SqliteTraceCode.PROFILE] related event.
     */
    public class Profile(
        public val stmt: sqlite3_stmt,
        public val nanos: Long
    ) : SqliteTraceEvent

    /**
     * [ksqlite.types.SqliteTraceCode.ROW] related event.
     */
    public class Row(public val stmt: sqlite3_stmt) : SqliteTraceEvent

    /**
     * [ksqlite.types.SqliteTraceCode.CLOSE] related event.
     */
    public class Close(public val db: sqlite3) : SqliteTraceEvent
}