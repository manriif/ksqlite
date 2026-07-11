package ksqlite.capi.vtab.callbacks

import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.types.SqliteResultCode

/**
 * This method notifies the virtual table implementation that the virtual table will be given a new
 * name. If this method returns SQLITE_OK then SQLite renames the table. If this method returns an
 * error code then the renaming is prevented.
 *
 * [The xRename Method](https://sqlite.org/vtab.html#the_xrename_method)
 */
public fun interface SqliteVtabRenameCallback<Vtab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xrename_method).
     */
    public fun apply(
        vTab: Vtab,
        newName: String
    ): SqliteResultCode.OkOrFailure
}