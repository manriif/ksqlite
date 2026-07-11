package ksqlite.kapi.database

import ksqlite.kapi.statement.PreparedStatement
import ksqlite.types.SqliteTraceEventCode

/**
 * Event passed as parameter to [Trace].
 */
public sealed interface TraceEvent {

    /**
     * [SqliteTraceEventCode.STMT] related event.
     */
    public class Stmt(
        public val statement: PreparedStatement,
        public val sql: String
    ) : TraceEvent

    /**
     * [SqliteTraceEventCode.PROFILE] related event.
     */
    public class Profile(
        public val statement: PreparedStatement,
        public val nanos: Long
    ) : TraceEvent

    /**
     * [SqliteTraceEventCode.ROW] related event.
     */
    public class Row(public val statement: PreparedStatement) : TraceEvent

    /**
     * [SqliteTraceEventCode.CLOSE] related event.
     */
    public class Close(public val connection: DatabaseConnection) : TraceEvent
}