package ksqlite.capi.vtab.callbacks

import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.types.SqliteResultCode

/**
 * Serves both for [SqliteVtabDestroyCallback] and [SqliteVtabDisconnectCallback].
 */
public fun interface SqliteVtabDestroyOrDisconnectCallback<Vtab : sqlite3_vtab> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xdestroy_method)
     * and/or [here](https://sqlite.org/vtab.html#the_xdisconnect_method).
     */
    public fun apply(vTab: Vtab): SqliteResultCode.OkOrFailure
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
public typealias SqliteVtabDisconnectCallback<Vtab> = SqliteVtabDestroyOrDisconnectCallback<Vtab>


/**
 * This method releases a connection to a virtual table, just like the xDisconnect method, and it
 * also destroys the underlying table implementation. This method undoes the work of xCreate.
 *
 * [The xDestroy Method](https://sqlite.org/vtab.html#the_xdestroy_method)
 */
public typealias SqliteVtabDestroyCallback<Vtab> = SqliteVtabDestroyOrDisconnectCallback<Vtab>