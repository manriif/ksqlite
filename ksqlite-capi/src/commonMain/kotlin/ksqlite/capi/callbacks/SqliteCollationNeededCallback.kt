package ksqlite.capi.callbacks

import ksqlite.capi.types.sqlite3
import ksqlite.types.SqliteTextEncoding

/**
 * Callback to use with [ksqlite.capi.sqlite3_collation_needed].
 */
public fun interface SqliteCollationNeededCallback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/collation_needed.html).
     */
    public fun apply(
        appData: AppData,
        db: sqlite3,
        eTextRep: SqliteTextEncoding.CollationNeeded,
        name: String
    )
}