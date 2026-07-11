package ksqlite.types

/**
 * Event passed as third parameter to an SQLLOG callback.
 */
public sealed interface SqliteSqlLogEvent {

    /**
     * Database connection has just been opened.
     */
    public class DatabaseOpened(public val dbFileName: String) : SqliteSqlLogEvent

    /**
     * Statement has just been executed.
     */
    public class StatementExecuted(public val statement: String) : SqliteSqlLogEvent

    /**
     * Database connection is being closed.
     */
    public object DatabaseClosed : SqliteSqlLogEvent
}