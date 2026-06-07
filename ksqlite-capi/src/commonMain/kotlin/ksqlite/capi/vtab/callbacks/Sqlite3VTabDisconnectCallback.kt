package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * This method releases a connection to a virtual table. Only the sqlite3_vtab object is destroyed.
 * The virtual table is not destroyed and any backing store associated with the virtual table
 * persists. This method undoes the work of xConnect.
 *
 * [The xDisconnect Method](https://sqlite.org/vtab.html#the_xdisconnect_method)
 */
public fun interface Sqlite3VTabDisconnectCallback<VTab: sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xdisconnect_method).
     */
    public fun handle(vTab: VTab): Sqlite3Result.OkOrFailure
}