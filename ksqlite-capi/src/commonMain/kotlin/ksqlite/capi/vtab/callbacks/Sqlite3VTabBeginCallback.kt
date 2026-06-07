package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * This method begins a transaction on a virtual table. This method is optional. The xBegin pointer
 * of sqlite3_module may be NULL.
 *
 * This method is always followed by one call to either the xCommit or xRollback method. Virtual
 * table transactions do not nest, so the xBegin method will not be invoked more than once on a
 * single virtual table without an intervening call to either xCommit or xRollback. Multiple calls
 * to other methods can and likely will occur in between the xBegin and the corresponding xCommit or
 * xRollback.
 *
 * [The xBegin Method](https://sqlite.org/vtab.html#the_xbegin_method)
 */
public fun interface Sqlite3VTabBeginCallback<VTab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xbegin_method).
     */
    public fun handle(vTab: VTab): Sqlite3Result.OkOrFailure
}