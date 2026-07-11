package ksqlite.kapi.database

import ksqlite.kapi.SQLite
import ksqlite.kapi.SQLiteException

/**
 * Callback to use with [SQLite.addAutoExtension] and [SQLite.removeAutoExtension].
 */
public fun interface AutoExtension {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/auto_extension.html).
     *
     * If an error is detected, then an [SQLiteException] should be thrown. Other exception types
     * are not caught.
     */
    public fun apply(connection: DatabaseConnection)
}