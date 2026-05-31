package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * Serves both for [Sqlite3VTabCreateCallback] and [Sqlite3VTabConnectCallback].
 */
public fun interface Sqlite3VTabCreateOrConnectCallback<AppData, VTab : sqlite3_vtab> {

    /**
     * Result for [handle].
     */
    public sealed interface Result<VTab : sqlite3_vtab>

    /**
     * Scope for [handle].
     */
    public sealed interface Scope<VTab : sqlite3_vtab> {

        /**
         * Writes [vTab] to `ppVtab` and returns [Sqlite3Result.OK] to SQLite.
         */
        public fun success(vTab: VTab): Result<VTab>

        /**
         * Writes [error] to `pzErr` and returns [Sqlite3Result.ERROR] to SQLite.
         */
        public fun failure(error: String): Result<VTab>
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xcreate_method)
     * and/or [here](https://sqlite.org/vtab.html#the_xconnect_method).
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope<VTab>.handle(
        db: sqlite3,
        appData: AppData,
        argv: Array<String>
    ): Result<VTab>
}

///////////////////////////////////////////////////////////////////////////
// Aliases
///////////////////////////////////////////////////////////////////////////

/**
 * The xCreate method is called to create a new instance of a virtual table in response to a CREATE
 * VIRTUAL TABLE statement. If the xCreate method is the same pointer as the xConnect method, then
 * the virtual table is an eponymous virtual table. If the xCreate method is omitted (if it is a
 * NULL pointer) then the virtual table is an eponymous-only virtual table.
 *
 * [The xCreate Method](https://sqlite.org/vtab.html#the_xcreate_method)
 */
public typealias Sqlite3VTabCreateCallback<AppData, VTab> =
        Sqlite3VTabCreateOrConnectCallback<AppData, VTab>

/**
 * The xConnect method is very similar to xCreate. It has the same parameters and constructs a new
 * sqlite3_vtab structure just like xCreate. And it must also call sqlite3_declare_vtab() like
 * xCreate. It should also make all of the same sqlite3_vtab_config() calls as xCreate.
 *
 * The difference is that xConnect is called to establish a new connection to an existing virtual
 * table whereas xCreate is called to create a new virtual table from scratch.
 *
 * [The xConnect Method](https://sqlite.org/vtab.html#the_xconnect_method)
 */
public typealias Sqlite3VTabConnectCallback<AppData, VTab> =
        Sqlite3VTabCreateOrConnectCallback<AppData, VTab>