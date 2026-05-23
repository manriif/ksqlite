package ksqlite.capi.callbacks

import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.sqlite3

/**
 * Callback to use with [ksqlite.capi.sqlite3_collation_needed].
 */
public fun interface Sqlite3CollationNeededCallback<AppData> {

    /**
     * Details on parameters can be found [here](https://sqlite.org/c3ref/collation_needed.html).
     */
    public fun handle(
        appData: AppData,
        db: sqlite3,
        eTextRep: Sqlite3TextEncoding.Set2,
        name: String
    )
}