@file:Suppress("SpellCheckingInspection", "ClassName")

package ksqlite.types

/**
 * These constants are the available integer "verbs" that can be passed as the second argument to
 * the sqlite3_db_status() interface.
 *
 * [Status Parameters for database connections](https://sqlite.org/c3ref/c_dbstatus_options.html)
 */
public enum class Sqlite3DbStatusOption(internal val id: Int) {

    /**
     * This parameter returns the number of lookaside memory slots currently checked out.
     */
    LOOKASIDE_USED(0),

    /**
     * This parameter returns the approximate number of bytes of heap memory used by all pager
     * caches associated with the database connection. The highwater mark associated with
     * SQLITE_DBSTATUS_CACHE_USED is always 0.
     */
    CACHE_USED(1),

    /**
     * This parameter returns the approximate number of bytes of heap memory used to store the
     * schema for all databases associated with the connection - main, temp, and any ATTACH-ed
     * databases. The full amount of memory used by the schemas is reported, even if the schema
     * memory is shared with other database connections due to shared cache mode being enabled.
     * The highwater mark associated with SQLITE_DBSTATUS_SCHEMA_USED is always 0.
     */
    SCHEMA_USED(2),

    /**
     * This parameter returns the approximate number of bytes of heap and lookaside memory used by
     * all prepared statements associated with the database connection. The highwater mark
     * associated with SQLITE_DBSTATUS_STMT_USED is always 0.
     */
    STMT_USED(3),

    /**
     * This parameter returns the number of malloc attempts that were satisfied using lookaside
     * memory. Only the high-water value is meaningful; the current value is always zero.
     */
    LOOKASIDE_HIT(4),

    /**
     * This parameter returns the number of malloc attempts that might have been satisfied using
     * lookaside memory but failed due to the amount of memory requested being larger than the
     * lookaside slot size. Only the high-water value is meaningful; the current value is always
     * zero.
     */
    LOOKASIDE_MISS_SIZE(5),

    /**
     * This parameter returns the number of malloc attempts that might have been satisfied using
     * lookaside memory but failed due to all lookaside memory already being in use. Only the
     * high-water value is meaningful; the current value is always zero.
     */
    LOOKASIDE_MISS_FULL(6),

    /**
     * This parameter returns the number of pager cache hits that have occurred. The highwater mark
     * associated with SQLITE_DBSTATUS_CACHE_HIT is always 0.
     */
    CACHE_HIT(7),

    /**
     * This parameter returns the number of pager cache misses that have occurred. The highwater
     * mark associated with SQLITE_DBSTATUS_CACHE_MISS is always 0.
     */
    CACHE_MISS(8),

    /**
     * This parameter returns the number of dirty cache entries that have been written to disk.
     * Specifically, the number of pages written to the wal file in wal mode databases, or the
     * number of pages written to the database file in rollback mode databases. Any pages written as
     * part of transaction rollback or database recovery operations are not included. If an IO or
     * other error occurs while writing a page to disk, the effect on subsequent
     * SQLITE_DBSTATUS_CACHE_WRITE requests is undefined. The highwater mark associated with
     * SQLITE_DBSTATUS_CACHE_WRITE is always 0.
     *
     * There is overlap between the quantities measured by this parameter
     * (SQLITE_DBSTATUS_CACHE_WRITE) and SQLITE_DBSTATUS_TEMPBUF_SPILL. Resetting one will reduce
     * the other.
     */
    CACHE_WRITE(9),

    /**
     * This parameter returns zero for the current value if and only if all foreign key constraints
     * (deferred or immediate) have been resolved. The highwater mark is always 0.
     */
    DEFERRED_FKS(10),

    /**
     * This parameter is similar to DBSTATUS_CACHE_USED, except that if a pager cache is shared 
     * between two or more connections the bytes of heap memory used by that pager cache is divided 
     * evenly between the attached connections. In other words, if none of the pager caches
     * associated with the database connection are shared, this request returns the same value as 
     * DBSTATUS_CACHE_USED. Or, if one or more of the pager caches are shared, the value returned by
     * this call will be smaller than that returned by DBSTATUS_CACHE_USED. The highwater mark 
     * associated with SQLITE_DBSTATUS_CACHE_USED_SHARED is always 0.
     */
    CACHE_USED_SHARED(11),

    /**
     * This parameter returns the number of dirty cache entries that have been written to disk in
     * the middle of a transaction due to the page cache overflowing. Transactions are more
     * efficient if they are written to disk all at once. When pages spill mid-transaction, that
     * introduces additional overhead. This parameter can be used to help identify inefficiencies
     * that can be resolved by increasing the cache size.
     */
    CACHE_SPILL(12),

    /**
     * This parameter returns the number of bytes written to temporary files on disk that could have
     * been kept in memory had sufficient memory been available. This value includes writes to
     * intermediate tables that are part of complex queries, external sorts that spill to disk, and
     * writes to TEMP tables. The highwater mark is always 0.
     */
    TEMPBUF_SPILL(13),
}