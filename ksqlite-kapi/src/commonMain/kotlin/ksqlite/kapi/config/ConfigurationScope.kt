/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:Suppress("SpellCheckingInspection")

package ksqlite.kapi.config

/**
 * The full set of SQLite's [configuration options](https://sqlite.org/c3ref/c_config_covering_index_scan.html),
 * available only inside the `configure` block passed to [ksqlite.kapi.SQLite.initialize], before
 * SQLite is initialized. [AnyTimeConfiguration] exposes the smaller subset still changeable
 * afterward.
 */
public interface ConfigurationScope : AnyTimeConfiguration {

    /**
     * Whether views may have a ROWID.
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
     * Sets the [SqlLogger] that receives every connection's open, statement execution and close
     * events for the rest of the SQLite session, or `null` to stop logging.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setSqlLogger(logger: SqlLogger?)

    /**
     * Sets the default mmap size limit, [sz], and the maximum allowed mmap size limit, [mx], for
     * every connection opened from now on.
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
     * Sets whether SQLite should avoid large memory allocations if possible.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setSmallMallocEnabled(enabled: Boolean)

    /**
     * Sets the default maximum size for an in-memory database created by deserializing a
     * previously serialized one.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setInMemoryDatabaseMaxSize(maxSize: Long)
}