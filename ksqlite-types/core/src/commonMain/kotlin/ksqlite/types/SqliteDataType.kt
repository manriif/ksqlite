package ksqlite.types

/**
 * Every value in SQLite has one of five fundamental datatypes:
 * These constants are codes for each of those types.
 *
 * [Fundamental Datatypes](https://sqlite.org/c3ref/c_blob.html)
 */
public sealed class SqliteDataType(public val code: Int) {

    /**
     * Any data type different from [NULL].
     */
    public sealed class NotNull(code: Int) : SqliteDataType(code)

    /**
     * 64-bit signed integer.
     */
    public data object INTEGER : NotNull(1)

    /**
     * 64-bit IEEE floating point number.
     */
    public data object FLOAT : NotNull(2)

    /**
     * String.
     */
    public data object TEXT : NotNull(3)

    /**
     * BLOB.
     */
    public data object BLOB : NotNull(4)

    /**
     * NULL.
     */
    public data object NULL : SqliteDataType(5)
}