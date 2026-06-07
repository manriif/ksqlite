package ksqlite.capi.vtab

/**
 * Virtual table implementations are allowed to set the sqlite3_index_info.idxFlags field to some
 * combination of these bits.
 *
 * [Virtual Table Scan Flags](https://sqlite.org/c3ref/c_index_scan_hex.html)
 */
public sealed class Sqlite3VTabScanFlag(internal open val value: Int) {

    /**
     * Scan visits at most 1 row.
     */
    public data object UNIQUE : Sqlite3VTabScanFlag(0x00000001)

    /**
     * Display idxNum as hex in EXPLAIN QUERY PLAN.
     */
    public data object HEX : Sqlite3VTabScanFlag(0x00000002)

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holder for the flags to be passed to the serialize API function.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        Sqlite3VTabScanFlag(value) {

        override fun contains(flag: Sqlite3VTabScanFlag): Boolean {
            return (value and flag.value) == flag.value
        }
    }

    /**
     * Returns an [Sqlite3VTabScanFlag] which is ORed with [flag].
     */
    public infix fun or(flag: Sqlite3VTabScanFlag): Sqlite3VTabScanFlag {
        return Mask(value or flag.value)
    }

    /**
     * Returns an [Sqlite3VTabScanFlag] which is ANDed with [flag].
     */
    public infix fun and(flag: Sqlite3VTabScanFlag): Sqlite3VTabScanFlag {
        return Mask(value and flag.value)
    }

    /**
     * Returns `true` if [flag] is equals to `this`.
     * It this is a mask, returns `true` if it contains [flag].
     */
    public open operator fun contains(flag: Sqlite3VTabScanFlag): Boolean {
        return flag == this || flag.value == value
    }
}