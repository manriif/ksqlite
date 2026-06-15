package ksqlite.capi.callbacks

import ksqlite.capi.types.sqlite3
import ksqlite.types.SqliteSqlLogEvent

/**
 * Callback to use with [ksqlite.capi.types.SqliteConfigOption.SQLLOG].
 */
public fun interface SqliteConfigSqlLogCallback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/c_config_covering_index_scan.html).
     */
    public fun apply(
        appData: AppData,
        db: sqlite3,
        event: SqliteSqlLogEvent
    )
}