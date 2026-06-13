package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * Serves both for [Sqlite3VTabDestroyCallback] and [Sqlite3VTabDisconnectCallback].
 */
public fun interface Sqlite3VTabDestroyOrDisconnectCallback<VTab: sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xdestroy_method)
     * and/or [here](https://sqlite.org/vtab.html#the_xdisconnect_method).
     */
    public fun handle(vTab: VTab): Sqlite3Result.OkOrFailure
}

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

/**
 * This method releases a connection to a virtual table. Only the sqlite3_vtab object is destroyed.
 * The virtual table is not destroyed and any backing store associated with the virtual table
 * persists. This method undoes the work of xConnect.
 *
 * [The xDisconnect Method](https://sqlite.org/vtab.html#the_xdisconnect_method)
 */
public typealias Sqlite3VTabDisconnectCallback<VTab> = Sqlite3VTabDestroyOrDisconnectCallback<VTab>


/**
 * This method releases a connection to a virtual table, just like the xDisconnect method, and it
 * also destroys the underlying table implementation. This method undoes the work of xCreate.
 *
 * [The xDestroy Method](https://sqlite.org/vtab.html#the_xdestroy_method)
 */
public typealias Sqlite3VTabDestroyCallback<VTab> = Sqlite3VTabDestroyOrDisconnectCallback<VTab>