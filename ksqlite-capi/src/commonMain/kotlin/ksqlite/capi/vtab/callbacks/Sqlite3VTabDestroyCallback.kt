package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * This method releases a connection to a virtual table, just like the xDisconnect method, and it
 * also destroys the underlying table implementation. This method undoes the work of xCreate.
 *
 * [The xDestroy Method](https://sqlite.org/vtab.html#the_xdestroy_method)
 */
public fun interface Sqlite3VTabDestroyCallback<VTab: sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xdestroy_method).
     */
    public fun handle(vTab: VTab): Sqlite3Result.OkOrFailure
}