package ksqlite.kapi.functions

import ksqlite.kapi.SQLiteConnection

/**
 * Supplies the necessary APIs during the invocation of a function hook.
 */
public interface FunctionScope {

    /**
     * Returns the database connection associated with the hook.
     */
    public val connection: SQLiteConnection
}