package ksqlite.capi.vtab.callbacks

import ksqlite.types.SqliteResultCode
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * This method notifies the virtual table implementation that the virtual table will be given a new
 * name. If this method returns SQLITE_OK then SQLite renames the table. If this method returns an
 * error code then the renaming is prevented.
 *
 * [The xRename Method](https://sqlite.org/vtab.html#the_xrename_method)
 */
public fun interface SqliteVTabRenameCallback<VTab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xrename_method).
     */
    public fun handle(
        vTab: VTab,
        newName: String
    ): SqliteResultCode.OkOrFailure
}