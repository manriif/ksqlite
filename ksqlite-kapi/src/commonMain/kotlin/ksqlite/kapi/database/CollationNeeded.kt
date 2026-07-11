package ksqlite.kapi.database

import ksqlite.types.SqliteTextEncoding

/**
 * Callback to use with [DatabaseConnection.setCollationNeeded].
 */
public fun interface CollationNeeded {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/collation_needed.html).
     */
    public fun apply(
        connection: DatabaseConnection,
        encoding: SqliteTextEncoding.CollationNeeded,
        name: String
    )
}