package ksqlite.capi.vtab.callbacks

import ksqlite.types.SqliteResultCode
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * SQLite uses the xBestIndex method of a virtual table module to determine the best way to access
 * the virtual table.
 *
 * [The xBestIndex Method](https://sqlite.org/vtab.html#the_xbestindex_method)
 */
public fun interface SqliteVTabBestIndexCallback<VTab: sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xbestindex_method).
     */
    public fun handle(
        vTab: VTab,
        info: sqlite3_index_info
    ): SqliteResultCode.OkOrFailure
}