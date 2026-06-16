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
    ReadWrite,

    /**
     * The database is readonly.
     */
    ReadOnly,

    /**
     * The database is not part of the connection.
     */
    UnknownDatabase
}