package ksqlite.types

/**
 * Every value in SQLite has one of five fundamental datatypes:
 * These constants are codes for each of those types.
 *
 * [Fundamental Datatypes](https://sqlite.org/c3ref/c_blob.html)
 */
public enum class SqliteDataType(public val code: Int) {

    /**
     * 64-bit signed integer.
     */
    INTEGER(1),

    /**
     * 64-bit IEEE floating point number.
     */
    FLOAT(2),

    /**
     * String.
     */
    TEXT(3),

    /**
     * BLOB.
     */
    BLOB(4),

    /**
     * NULL.
     */
    NULL(5)
}