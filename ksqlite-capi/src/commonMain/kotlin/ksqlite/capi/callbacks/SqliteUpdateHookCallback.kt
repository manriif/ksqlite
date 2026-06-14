package ksqlite.capi.callbacks

import ksqlite.types.SqliteActionCode

/**
 * Callback to use with [ksqlite.capi.sqlite3_update_hook].
 */
public fun interface SqliteUpdateHookCallback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/update_hook.html).
     */
    public fun apply(
        appData: AppData,
        action: SqliteActionCode.Dml,
        dbName: String,
        tableName: String,
        rowId: Long
    )
}