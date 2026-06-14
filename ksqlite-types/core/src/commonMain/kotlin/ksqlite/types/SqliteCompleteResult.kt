package ksqlite.types

/**
 * Result for `sqlite3_complete()`.
 *
 * [sqlite3_complete()](https://sqlite.org/c3ref/complete.html).
 */
public sealed interface SqliteCompleteResult {

    /**
     * The input string appears to be a complete SQL statement.
     */
    public data object Complete : SqliteCompleteResult

    /**
     * The statement is incomplete.
     */
    public data object Incomplete : SqliteCompleteResult

    /**
     * A failure occurred.
     */
    public data class Failure(public val result: SqliteResultCode.Failure) : SqliteCompleteResult
}