package ksqlite.kapi.function

import ksqlite.types.SqliteResultCode
import ksqlite.kapi.database.DatabaseConnection
import ksqlite.kapi.SQLiteException

/**
 * Supplies the necessary APIs during the invocation of a function hook.
 *
 * If an error is detected, an [SQLiteException] can be thrown. The error is then returned to
 * SQLite.
 * Only [SQLiteException] are recognized as normal error and other exceptions types are not caught.
 * It is also possible to use any of [setResultError], [setResultErrorNoMem] or
 * [setResultErrorTooBig].
 */
public interface FunctionScope {

    /**
     * Returns the database connection associated with the hook.
     */
    public val connection: DatabaseConnection

    /**
     * Causes SQLite to throw an exception with [message].
     * The default error code is [SqliteResultCode.ERROR] but can be overriden by supplying the appropriate error code
     *
     * By default, SQLite sets the error code to [SqliteResultCode.ERROR] but it can be overridden by
     * supplying an appropriate error [result].
     *
     * This method is equivalent to throwing an [SQLiteException].
     */
    public fun setResultError(
        message: String,
        result: SqliteResultCode.Failure = SqliteResultCode.ERROR
    ): Nothing

    /**
     * Causes SQLite to throw an error indicating that a memory allocation failed.
     */
    public fun setResultErrorNoMem(): Nothing

    /**
     * Causes SQLite to throw an error indicating that a string or BLOB is too long to represent.
     */
    public fun setResultErrorTooBig(): Nothing
}