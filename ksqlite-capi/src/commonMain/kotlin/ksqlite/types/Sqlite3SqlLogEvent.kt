@file:Suppress("ClassName")

package ksqlite.types

/**
 * Event passed as third parameter to [Sqlite3SqlLogCallback].
 */
public sealed interface Sqlite3SqlLogEvent {

    /**
     * The database connection on which the event happened
     */
    public val db: Sqlite3Param<sqlite3>

    /**
     * Database connection has just been opened.
     */
    public class DatabaseOpened(
        override val db: Sqlite3Param<sqlite3>,
        public val dbFileName: Sqlite3Param<String>
    ) : Sqlite3SqlLogEvent

    /**
     * Statement has just been executed.
     */
    public class StatementExecuted(
        override val db: Sqlite3Param<sqlite3>,
        public val statement: String
    ) : Sqlite3SqlLogEvent

    /**
     * Database connection is being closed.
     */
    public class DatabaseClosed(override val db: Sqlite3Param<sqlite3>) : Sqlite3SqlLogEvent
}