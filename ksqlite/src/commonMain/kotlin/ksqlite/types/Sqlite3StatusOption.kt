@file:Suppress("SpellCheckingInspection", "ClassName")

package ksqlite.types

/**
 * These integer constants designate various run-time status parameters that can be returned by
 * sqlite3_status().
 *
 * [Status Parameters](https://sqlite.org/c3ref/c_status_malloc_count.html)
 */
public enum class Sqlite3StatusOption(internal val id: Int) {

    /**
     * This parameter is the current amount of memory checked out using sqlite3_malloc(), either
     * directly or indirectly. The figure includes calls made to sqlite3_malloc() by the application
     * and internal memory usage by the SQLite library. Auxiliary page-cache memory controlled by
     * SQLITE_CONFIG_PAGECACHE is not included in this parameter. The amount returned is the sum of
     * the allocation sizes as reported by the xSize method in sqlite3_mem_methods.
     */
    MEMORY_USED(0),

    /**
     * This parameter returns the number of pages used out of the pagecache memory allocator that
     * was configured using SQLITE_CONFIG_PAGECACHE. The value returned is in pages, not in bytes.
     */
    PAGECACHE_USED(1),

    /**
     * This parameter returns the number of bytes of page cache allocation which could not be
     * satisfied by the SQLITE_CONFIG_PAGECACHE buffer and where forced to overflow to
     * sqlite3_malloc(). The returned value includes allocations that overflowed because they were
     * too large (they were larger than the "sz" parameter to SQLITE_CONFIG_PAGECACHE) and
     * allocations that overflowed because no space was left in the page cache.
     */
    PAGECACHE_OVERFLOW(2),

    /**
     * No longer used.
     */
    SCRATCH_USED(3),

    /**
     * No longer used.
     */
    SCRATCH_OVERFLOW(4),

    /**
     * This parameter records the largest memory allocation request handed to sqlite3_malloc() or
     * sqlite3_realloc() (or their internal equivalents). Only the value returned in the *pHighwater
     * parameter to sqlite3_status() is of interest. The value written into the *pCurrent parameter
     * is undefined.
     */
    MALLOC_SIZE(5),

    /**
     * The *pHighwater parameter records the deepest parser stack. The *pCurrent value is undefined.
     * The *pHighwater value is only meaningful if SQLite is compiled with YYTRACKMAXSTACKDEPTH.
     */
    PARSER_STACK(6),

    /**
     * This parameter records the largest memory allocation request handed to the pagecache memory
     * allocator. Only the value returned in the *pHighwater parameter to sqlite3_status() is of
     * interest. The value written into the *pCurrent parameter is undefined.
     */
    PAGECACHE_SIZE(7),

    /**
     * No longer used.
     */
    SCRATCH_SIZE(8),

    /**
     * This parameter records the number of separate memory allocations currently checked out.
     */
    MALLOC_COUNT(9),
}