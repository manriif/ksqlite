package ksqlite.types.vfs

/**
 * Virtual file system structure version number.
 */
public enum class SqliteVfsVersion(public val iVersion: Int) {

    /**
     * Initial version.
     */
    VERSION_1(1),

    /**
     * Adds support for xCurrentTimeInt64.
     */
    VERSION_2(2),

    /**
     * Adds support for xSetSystemCall, xGetSystemCall and xNextSystemCall.
     */
    VERSION_3(3)
}