package ksqlite.capi.vtab.callbacks

import ksqlite.types.SqliteResultCode
import ksqlite.capi.types.sqlite3_value
import ksqlite.capi.vtab.sqlite3_vtab_cursor

/**
 * This method begins a search of a virtual table. The first argument is a cursor opened by xOpen.
 * The next two arguments define a particular search index previously chosen by  xBestIndex. The
 * specific meanings of idxNum and idxStr are unimportant as long as xFilter and xBestIndex agree on
 * what that meaning is.
 *
 * [The xFilter Method](https://sqlite.org/vtab.html#the_xfilter_method)
 */
public fun interface SqliteVTabFilterCallback<VTabCursor : sqlite3_vtab_cursor> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/vtab.html#the_xfilter_method).
     */
    public fun handle(
        cursor: VTabCursor,
        idxNum: Int,
        idxStr: String?,
        arguments: Array<sqlite3_value>
    ): SqliteResultCode.OkOrFailure
}