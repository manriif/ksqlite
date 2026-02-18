package ksqlite.types

/**
 * Result of an SQLite C-API call returning a result integer.
 *
 * Many of the routines in the SQLite C-language Interface return numeric result codes indicating
 * either success or failure, and in the event of a failure, providing some idea of the cause of the
 * failure.
 *
 * [Result and Error Codes](https://sqlite.org/rescode.html)
 */
public sealed class Sqlite3Result(public val raw: Int) {

    /**
     * The SQLITE_OK result code means that the operation was successful and that there were no
     * errors. Most other result codes indicate an error.
     */
    public data object OK : Sqlite3Result(0)

    /**
     * The SQLITE_ROW result code returned by sqlite3_step() indicates that another row of output is
     * available.
     */
    public data object ROW : Sqlite3Result(100)

    /**
     * The SQLITE_DONE result code indicates that an operation has completed. The SQLITE_DONE result
     * code is most commonly seen as a return value from sqlite3_step() indicating that the SQL
     * statement has run to completion. But SQLITE_DONE can also be returned by other multi-step
     * interfaces such as sqlite3_backup_step().
     */
    public data object DONE : Sqlite3Result(101)

    /**
     * "Error codes" are a subset of "result codes" that indicate that something has gone wrong.
     */
    public class Error(raw: Int) : Sqlite3Result(raw) {

        /**
         * Returns the extracted [Sqlite3PrimaryResultCode] representing [raw] error code.
         */
        public val primaryCode: Sqlite3PrimaryResultCode
            get() = (raw and 0xFF).let { code ->
                Sqlite3PrimaryResultCode.entries.firstOrNull { it.raw == code }
                    ?: error("Unknown primary result code $code")
            }

        /**
         * Returns the [Sqlite3ExtendedResultCode] representing [raw] error code.
         *
         * Note that an [IllegalStateException] is thrown if sqlite was not configured to returns
         * extended result code.
         */
        public val extendedCode: Sqlite3ExtendedResultCode
            get() = Sqlite3ExtendedResultCode.entries.firstOrNull { it.raw == raw }
                ?: error("Unknown extended result code $raw")
    }
}