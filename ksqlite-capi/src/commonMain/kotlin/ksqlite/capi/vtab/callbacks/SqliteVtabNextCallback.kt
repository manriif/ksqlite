package ksqlite.capi.vtab.callbacks

import ksqlite.capi.vtab.sqlite3_vtab_cursor
import ksqlite.types.SqliteResultCode

/**
 * The xNext method advances a virtual table cursor to the next row of a result set initiated by
 * xFilter. If the cursor is already pointing at the last row when this routine is called, then the
 * cursor no longer points to valid data and a subsequent call to the xEof method must return true
 * (non-zero). If the cursor is successfully advanced to another row of content, then subsequent
 * calls to xEof must return false (zero).
 *
 * [The xNext Method](https://sqlite.org/vtab.html#the_xnext_method)
 */
public fun interface SqliteVtabNextCallback<VtabCursor : sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xnext_method).
     */
    public fun apply(cursor: VtabCursor): SqliteResultCode.OkOrFailure
}