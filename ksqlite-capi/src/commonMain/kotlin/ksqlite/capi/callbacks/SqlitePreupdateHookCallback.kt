package ksqlite.capi.callbacks

import ksqlite.capi.types.sqlite3
import ksqlite.types.SqliteActionCode

/**
 * Callback to use with [ksqlite.capi.sqlite3_preupdate_hook].
 */
public fun interface SqlitePreupdateHookCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/preupdate_blobwrite.html).
     */
    public fun apply(
        appData: AppData,
        db: sqlite3,
        action: SqliteActionCode.Dml,
        dbName: String,
        tableName: String,
        oldRowid: Long,
        newRowid: Long
    )
}