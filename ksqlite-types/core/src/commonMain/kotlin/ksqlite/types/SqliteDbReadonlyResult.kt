package ksqlite.types

/**
 * Result for `sqlite3_db_readonly()`.
 *
 * [Determine if a database is read-only](https://sqlite.org/c3ref/db_readonly.html).
 */
public enum class SqliteDbReadonlyResult {

    /**
     * The database is in read/write mode.
     */
    READWRITE,

    /**
     * The database is readonly.
     */
    READONLY,

    /**
     * The database is not part of the connection.
     */
    UNKNOWN_DATABASE
}