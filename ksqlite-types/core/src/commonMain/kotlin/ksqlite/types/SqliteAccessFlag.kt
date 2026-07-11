package ksqlite.types

/**
 * These integer constants can be used as the third parameter to the xAccess method of an
 * sqlite3_vfs object. They determine what kind of permissions the xAccess method is looking for.
 * With SQLITE_ACCESS_EXISTS, the xAccess method simply checks whether the file exists.
 *
 * [Flags for the xAccess VFS method](https://sqlite.org/c3ref/c_access_exists.html).
 */
public enum class SqliteAccessFlag(public open val value: Int) {

    /**
     * With SQLITE_ACCESS_READWRITE, the xAccess method checks whether the named directory is both
     * readable and writable (in other words, if files can be added, removed, and renamed within
     * the directory)
     */
    EXISTS(0),

    /**
     * The SQLITE_ACCESS_READWRITE constant is currently used only by the temp_store_directory
     * pragma, though this could change in a future release of SQLite
     */
    READWRITE(1),

    /**
     * With SQLITE_ACCESS_READ, the xAccess method checks whether the file is readable. The
     * SQLITE_ACCESS_READ constant is currently unused, though it might be used in a future release
     * of SQLite.
     */
    READ(2)
}