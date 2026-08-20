package ksqlite.kapi.statement

import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.types.SqliteExplainMode
import ksqlite.types.SqliteStatementStatusCounter

/**
 * Base for all prepared statement implementations.
 */
public sealed interface PreparedStatementBase {

    /**
     * Database connection to which this statement belongs.
     */
    public val connection: DatabaseConnection

    /**
     * Number of column in the result set.
     */
    public val columnCount: Int

    /**
     * SQL associated with the statement with bound parameters expanded.
     */
    public val expandedSql: String?

    /**
     * SQL text used to create this statement.
     */
    public val sql: String

    /**
     * Whether the prepared statement has been stepped at least once using [step] but has neither
     * run to completion (returned `null` from [step]) nor been reset.
     */
    public val isBusy: Boolean

    /**
     * Explain mode.
     */
    public val explain: SqliteExplainMode

    /**
     * Whether the prepared statement makes no direct changes to the content of the database file.
     */
    public val isReadOnly: Boolean

    /**
     * Returns the current value of the given [counter].
     */
    public fun getStatus(counter: SqliteStatementStatusCounter): Int
}