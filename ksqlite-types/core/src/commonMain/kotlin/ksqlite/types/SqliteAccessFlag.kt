package ksqlite.types

import ksqlite.types.vtab.SqliteVtabScanFlag

/**
 * These integer constants can be used as the third parameter to the xAccess method of an
 * sqlite3_vfs object. They determine what kind of permissions the xAccess method is looking for.
 * With SQLITE_ACCESS_EXISTS, the xAccess method simply checks whether the file exists.
 *
 * [Flags for the xAccess VFS method](https://sqlite.org/c3ref/c_access_exists.html).
 */
public sealed class SqliteAccessFlag(public open val value: Int) {

    /**
     * With SQLITE_ACCESS_READWRITE, the xAccess method checks whether the named directory is both
     * readable and writable (in other words, if files can be added, removed, and renamed within
     * the directory)
     */
    public data object EXISTS : SqliteAccessFlag(0)

    /**
     * The SQLITE_ACCESS_READWRITE constant is currently used only by the temp_store_directory
     * pragma, though this could change in a future release of SQLite
     */
    public data object READWRITE : SqliteAccessFlag(1)

    /**
     * With SQLITE_ACCESS_READ, the xAccess method checks whether the file is readable. The
     * SQLITE_ACCESS_READ constant is currently unused, though it might be used in a future release
     * of SQLite.
     */
    public data object READ : SqliteAccessFlag(2)

    ///////////////////////////////////////////////////////////////////////////
    // Masking
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Holder for the flags to be passed to the trace API function.
     */
    @ConsistentCopyVisibility
    public data class Mask internal constructor(override val value: Int) :
        SqliteAccessFlag(value) {

        override fun contains(flag: SqliteAccessFlag): Boolean =
            (value and flag.value) == flag.value
    }

    /**
     * Returns an [SqliteAccessFlag] which is ORed with [flag].
     */
    public infix fun or(flag: SqliteAccessFlag): SqliteAccessFlag =
        Mask(value or flag.value)

    /**
     * Returns an [SqliteAccessFlag] which is ANDed with [flag].
     */
    public infix fun and(flag: SqliteAccessFlag): SqliteAccessFlag =
        Mask(value and flag.value)

    /**
     * Returns an [SqliteAccessFlag] which has [flag] removed.
     */
    public infix fun without(flag: SqliteAccessFlag): SqliteAccessFlag =
        Mask(value and flag.value.inv())

    /**
     * Returns `true` if [flag] is equals to `this`.
     * It this is a mask, returns `true` if it contains [flag].
     */
    public open operator fun contains(flag: SqliteAccessFlag): Boolean =
        flag == this || flag.value == value

    public companion object {

        /**
         * Returns a [SqliteVtabScanFlag] from [value].
         */
        public fun from(value: Int): SqliteAccessFlag = Mask(value)
    }
}