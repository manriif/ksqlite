package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.vtab.sqlite3_vtab

/**
 * All changes to a virtual table are made using the xUpdate method. This one method can be used to
 * insert, delete, or update.
 *
 * [The xUpdate Method](https://sqlite.org/vtab.html#the_xupdate_method)
 */
public fun interface Sqlite3VTabUpdateCallback<VTab : sqlite3_vtab> {

    /**
     * Result for [handle].
     */
    public sealed interface Result

    /**
     * Scope for [handle].
     */
    public sealed interface Scope {

        /**
         * Writes [rowid] to `pRowid` if it is supplied and returns [Sqlite3Result.OK] to SQLite.
         */
        public fun success(rowid: Long?): Result

        /**
         * Returns [result] to SQLite.
         */
        public fun failure(result: Sqlite3Result.Failure): Result
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xupdate_method)
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope.handle(
        vTab: VTab,
        arguments: Array<sqlite3_value>
    ): Result
}