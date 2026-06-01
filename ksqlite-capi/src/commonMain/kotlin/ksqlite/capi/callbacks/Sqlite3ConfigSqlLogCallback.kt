package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3SqlLogEvent
import ksqlite.capi.types.sqlite3

/**
 * Callback to use with [ksqlite.capi.types.Sqlite3ConfigOption.SQLLOG].
 */
public fun interface Sqlite3ConfigSqlLogCallback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/c_config_covering_index_scan.html).
     */
    public fun apply(
        appData: AppData,
        db: sqlite3,
        event: Sqlite3SqlLogEvent
    )
}