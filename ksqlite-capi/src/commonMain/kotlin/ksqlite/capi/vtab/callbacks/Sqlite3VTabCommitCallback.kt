package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * This method causes a virtual table transaction to commit. This method is optional. The xCommit
 * pointer of sqlite3_module may be NULL.
 *
 * A call to this method always follows a prior call to xBegin and xSync.
 *
 * [The xCommit Method](https://sqlite.org/vtab.html#the_xcommit_method)
 */
public fun interface Sqlite3VTabCommitCallback<VTab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xcommit_method).
     */
    public fun handle(vTab: VTab): Sqlite3Result.OkOrFailure
}