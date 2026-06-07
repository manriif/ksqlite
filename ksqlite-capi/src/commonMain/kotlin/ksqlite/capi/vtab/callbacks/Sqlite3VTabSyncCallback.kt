package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * This method signals the start of a two-phase commit on a virtual table. This method is optional.
 * The xSync pointer of sqlite3_module may be NULL.
 *
 * This method is only invoked after a call to the xBegin method and prior to an xCommit or
 * xRollback. In order to implement two-phase commit, the xSync method on all virtual tables is
 * invoked prior to invoking the xCommit method on any virtual table. If any of the xSync methods
 * fail, the entire transaction is rolled back.
 *
 * [The xSync Method](https://sqlite.org/vtab.html#the_xsync_method)
 */
public fun interface Sqlite3VTabSyncCallback<VTab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xsync_method).
     */
    public fun handle(vTab: VTab): Sqlite3Result.OkOrFailure
}