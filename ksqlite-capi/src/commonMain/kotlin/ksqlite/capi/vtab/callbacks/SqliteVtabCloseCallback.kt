package ksqlite.capi.vtab.callbacks

import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.types.SqliteResultCode

/**
 * The xClose method closes a cursor previously opened by xOpen. The SQLite core will always call
 * xClose once for each cursor opened using xOpen.
 *
 * [The xClose Method](https://sqlite.org/vtab.html#the_xclose_method)
 */
public fun interface SqliteVtabCloseCallback<VtabCursor : sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xclose_method).
     *
     * Note that it is the responsibility of the [cursor] allocator to deallocate it.
     */
    public fun apply(cursor: VtabCursor): SqliteResultCode.OkOrFailure
}