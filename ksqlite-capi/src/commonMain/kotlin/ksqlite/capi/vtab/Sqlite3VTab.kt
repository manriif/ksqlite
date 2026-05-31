package ksqlite.capi.vtab

/**
 * Definition of [sqlite3_vtab].
 */
public expect abstract class Sqlite3VTab() {

    /**
     * Number of open cursor.
     */
    public val nRef: Int

    /**
     * Error message that can be set from virtual table methods.
     * Setting a value to this field automatically free any previously existing value.
     */
    public var errMsg: String?
}