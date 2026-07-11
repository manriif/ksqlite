package ksqlite.capi.vtab.callbacks

import ksqlite.capi.sqlite3_value
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.types.SqliteResultCode

/**
 * All changes to a virtual table are made using the xUpdate method. This one method can be used to
 * insert, delete, or update.
 *
 * [The xUpdate Method](https://sqlite.org/vtab.html#the_xupdate_method)
 */
public fun interface SqliteVtabUpdateCallback<Vtab : sqlite3_vtab> {

    /**
     * Result for [apply].
     */
    public sealed interface Result

    /**
     * Scope for [apply].
     */
    public sealed interface Scope {

        /**
         * Writes [rowid] to `pRowid` if it is supplied and returns [SqliteResultCode.OK] to SQLite.
         */
        public fun success(rowid: Long?): Result

        /**
         * Returns [result] to SQLite.
         */
        public fun failure(result: SqliteResultCode.Failure): Result
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xupdate_method)
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope.apply(
        vTab: Vtab,
        arguments: Array<sqlite3_value>
    ): Result
}