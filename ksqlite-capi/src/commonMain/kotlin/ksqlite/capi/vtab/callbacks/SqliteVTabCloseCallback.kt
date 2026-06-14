package ksqlite.capi.vtab.callbacks

import ksqlite.types.SqliteResultCode
import ksqlite.capi.vtab.sqlite3_vtab_cursor

/**
 * The xClose method closes a cursor previously opened by xOpen. The SQLite core will always call
 * xClose once for each cursor opened using xOpen.
 *
 * [The xClose Method](https://sqlite.org/vtab.html#the_xclose_method)
 */
public fun interface SqliteVTabCloseCallback<VTabCursor: sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xclose_method).
     */
    public fun handle(cursor: VTabCursor): SqliteResultCode.OkOrFailure
}