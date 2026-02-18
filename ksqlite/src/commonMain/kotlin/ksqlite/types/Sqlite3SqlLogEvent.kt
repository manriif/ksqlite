@file:Suppress("ClassName")

package ksqlite.types

/**
 * Event passed as third parameter to [Sqlite3LogCallback].
 */
public sealed interface Sqlite3SqlLogEvent {

    /**
     * Database connection has just been opened.
     */
    public class DATABASE_OPENED(public val dbFileName: String)

    /**
     * Statement has just been executed.
     */
    public class STATEMENT_EXECUTED(public val statement: String)

    /**
     * Database connection is being closed.
     */
    public object DATABASE_CLOSED
}