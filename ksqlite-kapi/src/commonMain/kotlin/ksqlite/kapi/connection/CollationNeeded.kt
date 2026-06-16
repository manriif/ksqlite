package ksqlite.kapi.connection

import ksqlite.kapi.SQLiteException
import ksqlite.types.SqliteTextEncoding

/**
 * Callback to use with [Connection.setCollationNeeded].
 */
public fun interface CollationNeeded {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/auto_extension.html).
     *
     * If an error is detected, then an [SQLiteException] should be thrown. Other exception types
     * are not caught.
     */
    public fun apply(
        connection: Connection,
        encoding: SqliteTextEncoding.Set2,
        name: String
    )
}