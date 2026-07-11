package ksqlite.types.vfs

/**
 * IO structure version number.
 */
public enum class SqliteIoMethodsVersion(public val iVersion: Int) {

    /**
     * Initial version.
     */
    VERSION_1(1),

    /**
     * Adds support for xShmMap, xShmLock, xShmBarrier and xShmUnmap.
     */
    VERSION_2(2),

    /**
     * Adds support for xFetch and xUnfetch.
     */
    VERSION_3(3)
}