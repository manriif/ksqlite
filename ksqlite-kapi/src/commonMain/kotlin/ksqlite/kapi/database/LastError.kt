package ksqlite.kapi.database

import ksqlite.types.SqliteResultCode

/**
 * Provides access to the most recent database error information.
 *
 * Note that value returned by the member properties may change while the database connection is
 * used.
 */
public interface LastError {

    /**
     * UTF-8 encoded English language explanation.
     */
    public val message: String?

    /**
     * Error code.
     */
    public val code: SqliteResultCode

    /**
     * Extended error code.
     */
    public val extendedCode: SqliteResultCode

    /**
     * Byte offset of the start of the token specified by the input SQL, if referenced by the most
     * recent error.
     */
    public val offset: Int

    /**
     * OS-dependant error code or error number that caused the most recent I/O error or failure to
     * open a file.
     */
    public val systemError: Int

    /**
     * Sets the [code] to [errorCode] and [message] to [errorMessage].
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurs.
     */
    public fun update(
        errorCode: SqliteResultCode,
        errorMessage: String? = null
    )
}