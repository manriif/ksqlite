package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3SqlLogEvent
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_mutable_pointer

/**
 * Callback to use with [ksqlite.capi.types.Sqlite3ConfigOption.SQLLOG].
 */
public fun interface Sqlite3ConfigSqlLogCallback<ClientData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/c_config_covering_index_scan.html).
     */
    public fun handle(
        clientData: ClientData,
        db: sqlite3,
        event: Sqlite3SqlLogEvent
    )
}