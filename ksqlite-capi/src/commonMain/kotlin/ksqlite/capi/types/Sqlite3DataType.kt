package ksqlite.capi.types

/**
 * Every value in SQLite has one of five fundamental datatypes:
 * These constants are codes for each of those types.
 *
 * [Fundamental Datatypes](https://sqlite.org/c3ref/c_blob.html)
 */
public sealed class Sqlite3DataType(internal val code: Int) {

    /**
     * 64-bit signed integer.
     */
    public data object INTEGER : Sqlite3DataType(1)

    /**
     * 64-bit IEEE floating point number.
     */
    public data object FLOAT : Sqlite3DataType(2)

    /**
     * String.
     */
    public data object TEXT : Sqlite3DataType(3)

    /**
     * BLOB.
     */
    public data object BLOB : Sqlite3DataType(4)

    /**
     * NULL.
     */
    public data object NULL : Sqlite3DataType(5)
}

///////////////////////////////////////////////////////////////////////////
// Values
///////////////////////////////////////////////////////////////////////////

/**
 * Returns all [Sqlite3DataType]s.
 */
internal fun sqlite3DataTypes(): Set<Sqlite3DataType> = setOf(
    Sqlite3DataType.INTEGER,
    Sqlite3DataType.FLOAT,
    Sqlite3DataType.TEXT,
    Sqlite3DataType.BLOB,
    Sqlite3DataType.NULL
)