package ksqlite.capi.vtab.callbacks

import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.vtab.sqlite3_index_info
import ksqlite.capi.vtab.sqlite3_vtab
import ksqlite.capi.vtab.sqlite3_vtab_cursor

/**
 * The xClose method closes a cursor previously opened by xOpen. The SQLite core will always call
 * xClose once for each cursor opened using xOpen.
 *
 * [The xClose Method](https://sqlite.org/vtab.html#the_xclose_method)
 */
public fun interface Sqlite3VTabCloseCallback<VTabCursor: sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xclose_method).
     */
    public fun handle(cursor: VTabCursor): Sqlite3Result.OkOrFailure
}