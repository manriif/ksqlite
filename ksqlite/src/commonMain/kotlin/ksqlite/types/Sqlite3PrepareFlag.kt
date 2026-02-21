@file:Suppress("ClassName")

package ksqlite.types

/**
 * These constants define various flags that can be passed into the "prepFlags" parameter of the
 * sqlite3_prepare_v3() and sqlite3_prepare16_v3() interfaces.
 *
 * [Prepare Flags](https://sqlite.org/c3ref/c_prepare_dont_log.html)
 */
public sealed class Sqlite3PrepareFlag(internal open val value: Int) {

    /**
     * The SQLITE_PREPARE_PERSISTENT flag is a hint to the query planner that the prepared statement
     * will be retained for a long time and probably reused many times. Without this flag,
     * sqlite3_prepare_v3() and sqlite3_prepare16_v3() assume that the prepared statement will be
     * used just once or at most a few times and then destroyed using sqlite3_finalize() relatively
     * soon. The current implementation acts on this hint by avoiding the use of lookaside memory so
     * as not to deplete the limited store of lookaside memory. Future versions of SQLite may act on
     * this hint differently.
     */
    public data object PERSISTENT : Sqlite3PrepareFlag(0x01)

    /**
     * The SQLITE_PREPARE_NORMALIZE flag is a no-op. This flag used to be required for any prepared
     * statement that wanted to use the sqlite3_normalized_sql() interface. However, the
     * sqlite3_normalized_sql() interface is now available to all prepared statements, regardless of
     * whether or not they use this flag.
     */
    public data object NORMALIZE : Sqlite3PrepareFlag(0x02)

    /**
     * The SQLITE_PREPARE_NO_VTAB flag causes the SQL compiler to return an error (error code
     * SQLITE_ERROR) if the statement uses any virtual tables.
     */
    public data object NO_VTAB : Sqlite3PrepareFlag(0x04)

    /**
     * The SQLITE_PREPARE_DONT_LOG flag prevents SQL compiler errors from being sent to the error
     * log defined by SQLITE_CONFIG_LOG. This can be used, for example, to do test compiles to see
     * if some SQL syntax is well-formed, without generating messages on the global error log when
     * it is not. If the test compile fails, the sqlite3_prepare_v3() call returns the same error
     * indications with or without this flag; it just omits the call to sqlite3_log() that logs the
     * error.
     */
    public data object DONT_LOG : Sqlite3PrepareFlag(0x10)

    /**
     * Holder for the flags to be passed to the prepare API functions.
     */
    @ConsistentCopyVisibility
    public data class Masked internal constructor(override val value: Int) :
        Sqlite3PrepareFlag(value)

    /**
     * Returns an [Sqlite3PrepareFlag] which is ORed with [flag].
     */
    public infix fun or(flag: Sqlite3PrepareFlag): Sqlite3PrepareFlag {
        return Masked(value or flag.value)
    }
}