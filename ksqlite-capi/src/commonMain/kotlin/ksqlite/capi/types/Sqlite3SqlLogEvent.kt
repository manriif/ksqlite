@file:Suppress("ClassName")

package ksqlite.capi.types

/**
 * Event passed as third parameter to [Sqlite3ConfigSqlLogCallback].
 */
public sealed interface Sqlite3SqlLogEvent {

    /**
     * Database connection has just been opened.
     */
    public class DatabaseOpened(public val dbFileName: String) : Sqlite3SqlLogEvent

    /**
     * Statement has just been executed.
     */
    public class StatementExecuted(public val statement: String) : Sqlite3SqlLogEvent

    /**
     * Database connection is being closed.
     */
    public object DatabaseClosed : Sqlite3SqlLogEvent
}