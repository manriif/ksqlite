package ksqlite.capi.types

/**
 * Every value in SQLite has one of five fundamental datatypes:
 * These constants are codes for each of those types.
 *
 * [Fundamental Datatypes](https://sqlite.org/c3ref/c_blob.html)
 */
public sealed interface Sqlite3DataType {

    /**
     * 64-bit signed integer.
     */
    public data object INTEGER : Sqlite3DataType

    /**
     * 64-bit IEEE floating point number.
     */
    public data object FLOAT : Sqlite3DataType

    /**
     * String.
     */
    public data object TEXT : Sqlite3DataType

    /**
     * BLOB.
     */
    public data object BLOB : Sqlite3DataType

    /**
     * NULL.
     */
    public data object NULL : Sqlite3DataType
}