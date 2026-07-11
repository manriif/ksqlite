package ksqlite.types.vtab

/**
 * Virtual table implementations are allowed to set the sqlite3_index_info.idxFlags field to some
 * combination of these bits.
 *
 * [Virtual Table Scan Flags](https://sqlite.org/c3ref/c_index_scan_hex.html)
 */
public sealed class SqliteVtabScanFlag(public open val value: Int) {

    /**
     * Scan visits at most 1 row.
     */
    public data object UNIQUE : SqliteVtabScanFlag(0x00000001)

    /**
     * Display idxNum as hex in EXPLAIN QUERY PLAN.
     */
    public data object HEX : SqliteVtabScanFlag(0x00000002)

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holder for the flags to be passed to the serialize API function.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqliteVtabScanFlag(value) {

        override fun contains(flag: SqliteVtabScanFlag): Boolean =
            (value and flag.value) == flag.value
    }

    /**
     * Returns an [SqliteVtabScanFlag] which is ORed with [flag].
     */
    public infix fun or(flag: SqliteVtabScanFlag): SqliteVtabScanFlag =
        Mask(value or flag.value)

    /**
     * Returns an [SqliteVtabScanFlag] which is ANDed with [flag].
     */
    public infix fun and(flag: SqliteVtabScanFlag): SqliteVtabScanFlag =
        Mask(value and flag.value)

    /**
     * Returns an [SqliteVtabScanFlag] which has [flag] removed.
     */
    public infix fun without(flag: SqliteVtabScanFlag): SqliteVtabScanFlag =
        Mask(value and flag.value.inv())

    /**
     * Returns `true` if [flag] is equals to `this`.
     * It this is a mask, returns `true` if it contains [flag].
     */
    public open operator fun contains(flag: SqliteVtabScanFlag): Boolean =
        flag == this || flag.value == value

    public companion object {

        /**
         * Returns a [SqliteVtabScanFlag] from [value].
         */
        public fun from(value: Int): SqliteVtabScanFlag = Mask(value)
    }
}