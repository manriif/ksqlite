package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3ActionCode
import ksqlite.capi.types.sqlite3

/**
 * Callback to use with [ksqlite.capi.sqlite3_preupdate_hook].
 */
public fun interface Sqlite3PreupdateHookCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/preupdate_blobwrite.html).
     */
    public fun apply(
        appData: AppData,
        db: sqlite3,
        action: Sqlite3ActionCode.Dml,
        dbName: String,
        tableName: String,
        preRowId: Long,
        postRowId: Long
    )
}