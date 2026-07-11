@file:Suppress("SpellCheckingInspection")

package ksqlite.kapi.config

/**
 * Exposes the anytime options of the SQLite configuration API.
 *
 * [Configurations Options](https://sqlite.org/c3ref/c_config_covering_index_scan.html)
 */
public interface AnyTimeConfiguration {

    /**
     * Number of extra bytes per page required for each page in SQLITE_CONFIG_PAGECACHE.
     */
    public val pageCacheHeaderSize: Int

    /**
     * Sets the logging interface.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the option fails.
     */
    public fun setLogger(logger: Logger?)
}