package ksqlite.kapi.config

import ksqlite.kapi.database.DatabaseConnection
import ksqlite.types.SqliteSqlLogEvent

/**
 * SQLite SQL logging interface.
 */
public fun interface SqlLogger {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/c_config_covering_index_scan.html).
     */
    public fun log(
        connection: DatabaseConnection,
        event: SqliteSqlLogEvent
    )
}