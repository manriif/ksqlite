package ksqlite.kapi.database

import ksqlite.types.SqliteTextEncoding

/**
 * Callback to use with [DatabaseConnection.setCollationNeeded].
 */
public fun interface CollationNeeded {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/auto_extension.html).
     */
    public fun apply(
        connection: DatabaseConnection,
        encoding: SqliteTextEncoding.Set2,
        name: String
    )
}