@file:Suppress("SpellCheckingInspection")

package ksqlite.kapi.config

import ksqlite.kapi.buffer.Buffer

/**
 * Exposes SQLite configuration API.
 *
 * [Configurations Options](https://sqlite.org/c3ref/c_config_covering_index_scan.html)
 */
public interface ConfigurationScope : AnyTimeConfiguration {

    /**
     * Whether the ability for VIEWs to have ROWIDs is activated.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public var isRowidInViewActivated: Boolean

    /**
     * Sets the threading mode to Single-thread.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setSingleThread()

    /**
     * Sets the threading mode to Multi-thread.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setMultiThread()

    /**
     * Sets the threading mode to Serialized.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setSerialized()

    /**
     * Specifies a memory pool that SQLite can use for the database page cache with the default page
     * cache implementation.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setPageCacheConfig(
        pMem: Buffer?,
        sz: Int,
        n: Int
    )

    /**
     * Specifies a static memory buffer that SQLite will use for all of its dynamic memory
     * allocation needs beyond those provided for by [setPageCacheConfig].
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setHeapConfig(
        pMem: Buffer?,
        nBytes: Int,
        min: Int
    )

    /**
     * Enables or disables the collection of memory allocation statistics.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setMemStatusEnabled(enabled: Boolean)

    /**
     * Sets the default `lookaside` size.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setLookasideConfig(
        sz: Int,
        cnt: Int
    )

    /**
     * Enables or disables global URI handling.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setUriEnabled(enabled: Boolean)

    /**
     * Enables or disables the use of covering indices for full table scans in the query optimizer.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setCoveringIndexScanEnabled(enabled: Boolean)

    /**
     * Sets the logging interface for SQL logging.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setSqlLogger(logger: SqlLogger?)

    /**
     * Sets the default mmap size limit a,d the maximum allowed mmap size limit.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setMmapSize(
        sz: Long,
        mx: Long
    )

    /**
     * Sets the "Minimum PMA Size" for the multithreaded sorter to [szPma].
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setPackedMemoryArraySize(szPma: UInt)

    /**
     * Sets the statement journal spill-to-disk threshold.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setStatementJournalSpillThreshold(nByte: Int)

    /**
     * Sets wheter SQLite should avoid large memory allocations if possible.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setSmallMallocEnabled(enabled: Boolean)

    /**
     * Sets the new value of the sorter-reference size threshold.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setSorterReferenceSizeThreshold(nByte: Int)

    /**
     * Sets the default maximum size for an in-memory database created using `sqlite3_deserialize()`.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setInMemoryDatabaseMaxSize(maxSize: Long)
}