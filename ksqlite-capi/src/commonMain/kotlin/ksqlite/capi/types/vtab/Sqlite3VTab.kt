package ksqlite.capi.types.vtab

/**
 * Describes an [`sqlite3_vtab`](https://sqlite.org/c3ref/vtab.html) struct.
 */
public interface Sqlite3VTab {

    /**
     * Number of open cursor.
     */
    public val nRef: Int

    /**
     * Error message that can be set from virtual table methods.
     * Setting a value to this field automatically frees any previously existing value.
     */
    public var errMsg: String?
}