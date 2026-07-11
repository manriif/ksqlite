package ksqlite.capi.vtab.callbacks

import ksqlite.capi.vtab.sqlite3_vtab_cursor

/**
 * The xEof method must return false (zero) if the specified cursor currently points to a valid row
 * of data, or true (non-zero) otherwise. This method is called by the SQL engine immediately after
 * each xFilter and xNext invocation.
 *
 * [The xEof Method](https://sqlite.org/vtab.html#the_xeof_method)
 */
public fun interface SqliteVtabEofCallback<VtabCursor : sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xeof_method).
     */
    public fun apply(cursor: VtabCursor): Int
}