package ksqlite.kapi.statement

import ksqlite.kapi.database.DatabaseConnection

public interface Statement {

    /**
     * Database connection to which this statement belongs.
     */
    public val connection: DatabaseConnection

    /**
     * Returns the number of column in the result set.
     */
    public val columnCount: Int

    /**
     * Resets all host parameters to `null`.
     */
    public fun clearBindings()
}