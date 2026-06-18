package ksqlite.kapi.statement

import ksqlite.kapi.database.DatabaseConnection

public interface Statement : AutoCloseable {

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
     * Resets all host parameters to `null`.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun clear()

    /**
     * Resets the prepared statement object back to its initial state, making it ready to be
     * re-executed.
     *
     * Bindings values are not affected, [clear] must be used instead.
     *
     * @throws ksqlite.kapi.SQLiteException if the operation fails.
     */
    public fun reset()

    /**
     * Deletes a prepared statement.
     *
     * @throws ksqlite.kapi.SQLiteException if finalizing the statement fails.
     */
    override fun close()
}