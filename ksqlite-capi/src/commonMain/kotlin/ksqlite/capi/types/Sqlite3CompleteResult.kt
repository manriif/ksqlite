package ksqlite.capi.types

/**
 * Result for [ksqlite.capi.sqlite3_complete].
 *
 * [sqlite3_complete()](https://sqlite.org/c3ref/complete.html).
 */
public sealed interface Sqlite3CompleteResult {

    /**
     * The input string appears to be a complete SQL statement.
     */
    public data object Complete : Sqlite3CompleteResult

    /**
     * The statement is incomplete.
     */
    public data object Incomplete : Sqlite3CompleteResult

    /**
     * A failure occurred.
     */
    public data class Failure(public val result: Sqlite3Result.Failure) : Sqlite3CompleteResult
}