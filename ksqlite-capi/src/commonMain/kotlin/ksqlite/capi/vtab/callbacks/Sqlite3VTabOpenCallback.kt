package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.sqlite3
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.sqlite3_vtab_cursor

/**
 * The xOpen method creates a new cursor used for accessing (read and/or writing) a virtual  table.
 * A successful invocation of this method will allocate the memory for the sqlite3_vtab_cursor
 * (or a subclass), initialize the new object, and make *ppCursor point to the new object.
 *
 * [The xOpen Method](https://sqlite.org/vtab.html#the_xopen_method)
 */
public fun interface Sqlite3VTabOpenCallback<VTab : sqlite3_vtab, VTabCursor: sqlite3_vtab_cursor> {

    /**
     * Result for [handle].
     */
    public sealed interface Result<VTabCursor : sqlite3_vtab_cursor>

    /**
     * Scope for [handle].
     */
    public sealed interface Scope<VTabCursor : sqlite3_vtab_cursor> {

        /**
         * Writes [cursor] to `ppCursor` and returns [Sqlite3Result.OK] to SQLite.
         */
        public fun success(cursor: VTabCursor): Result<VTabCursor>

        /**
         * Returns [result] to SQLite.
         */
        public fun failure(result: Sqlite3Result.Failure): Result<VTabCursor>
    }

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xopen_method)
     *
     * A [Result] instance can be obtained by invoking one of [Scope.success] or [Scope.failure].
     */
    public fun Scope<VTabCursor>.handle(vTab: VTab): Result<VTabCursor>
}