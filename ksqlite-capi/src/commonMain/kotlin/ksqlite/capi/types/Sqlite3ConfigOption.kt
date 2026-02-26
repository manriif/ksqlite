@file:Suppress("SpellCheckingInspection", "ClassName")

package ksqlite.capi.types

/**
 * These constants are the available integer configuration options that can be passed as the first
 * argument to the sqlite3_config() interface.
 *
 * [Configuration Options][https://sqlite.org/c3ref/c_config_covering_index_scan.html]
 */
public sealed class Sqlite3ConfigOption(internal val id: Int) {

    /**
     * This option sets the threading mode to Single-thread. In other words, it disables all
     * mutexing and puts SQLite into a mode where it can only be used by a single thread. If SQLite
     * is compiled with the SQLITE_THREADSAFE=0 compile-time option then it is not possible to
     * change the threading mode from its default value of Single-thread and so sqlite3_config()
     * will return SQLITE_ERROR if called with the SQLITE_CONFIG_SINGLETHREAD configuration option.
     */
    public object SINGLETHREAD : Sqlite3ConfigOption(1)

    /**
     * This option sets the threading mode to Multi-thread. In other words, it disables mutexing on
     * database connection and prepared statement objects. The application is responsible for
     * serializing access to database connections and prepared statements. But other mutexes are
     * enabled so that SQLite will be safe to use in a multi-threaded environment as long as no two
     * threads attempt to use the same database connection at the same time. If SQLite is compiled
     * with the SQLITE_THREADSAFE=0 compile-time option then it is not possible to set the
     * Multi-thread threading mode and sqlite3_config() will return SQLITE_ERROR if called with the
     * SQLITE_CONFIG_MULTITHREAD configuration option.
     */
    public object MULTITHREAD : Sqlite3ConfigOption(2)

    /**
     * This option sets the threading mode to Serialized. In other words, this option enables all
     * mutexes including the recursive mutexes on database connection and prepared statement objects.
     * In this mode (which is the default when SQLite is compiled with SQLITE_THREADSAFE=1) the
     * SQLite library will itself serialize access to database connections and prepared statements
     * so that the application is free to use the same database connection or the same prepared
     * statement in different threads at the same time. If SQLite is compiled with the
     * SQLITE_THREADSAFE=0 compile-time option then it is not possible to set the Serialized
     * threading mode and sqlite3_config() will return SQLITE_ERROR if called with the
     * SQLITE_CONFIG_SERIALIZED configuration option.
     */
    public object SERIALIZED : Sqlite3ConfigOption(3)

    /**
     * The SQLITE_CONFIG_PAGECACHE option specifies a memory pool that SQLite can use for the
     * database page cache with the default page cache implementation. This configuration option is
     * a no-op if an application-defined page cache implementation is loaded using the
     * SQLITE_CONFIG_PCACHE2. There are three arguments to SQLITE_CONFIG_PAGECACHE: A pointer to
     * 8-byte aligned memory ([pMem]), the size of each page cache line ([sz]), and the number of
     * cache lines ([n]). The [sz] argument should be the size of the largest database page (a power
     * of two between 512 and 65536) plus some extra bytes for each page header. The number of extra
     * bytes needed by the page header can be determined using SQLITE_CONFIG_PCACHE_HDR[sz]. It is
     * harmless, apart from the wasted memory, for the [sz] parameter to be larger than necessary.
     * The [pMem] argument must be either a NULL pointer or a pointer to an 8-byte aligned block of
     * memory of at least [sz]*[n] bytes, otherwise subsequent behavior is undefined. When [pMem] is
     * not NULL, SQLite will strive to use the memory provided to satisfy page cache needs, falling
     * back to sqlite3_malloc() if a page cache line is larger than [sz] bytes or if all of the
     * [pMem] buffer is exhausted. If [pMem] is NULL and [n] is non-zero, then each database
     * connection does an initial bulk allocation for page cache memory from sqlite3_malloc()
     * sufficient for [n] cache lines if [n] is positive or of -1024*[n] bytes if [n] is negative.
     * If additional page cache memory is needed beyond what is provided by the initial allocation,
     * then SQLite goes to sqlite3_malloc() separately for each additional cache line.
     */
    public class PAGECACHE(
        internal val pMem: sqlite3_mutable_pointer?,
        internal val sz: Int,
        internal val n: Int
    ) : Sqlite3ConfigOption(7)

    /**
     * The SQLITE_CONFIG_HEAP option specifies a static memory buffer that SQLite will use for all
     * of its dynamic memory allocation needs beyond those provided for by SQLITE_CONFIG_PAGECACHE.
     * The SQLITE_CONFIG_HEAP option is only available if SQLite is compiled with either
     * SQLITE_ENABLE_MEMSYS3 or SQLITE_ENABLE_MEMSYS5 and returns SQLITE_ERROR if invoked otherwise.
     * There are three arguments to SQLITE_CONFIG_HEAP: An 8-byte aligned pointer to the memory,
     * the number of bytes in the memory buffer, and the minimum allocation size. If the first
     * pointer (the memory pointer) is NULL, then SQLite reverts to using its default memory
     * allocator (the system malloc() implementation), undoing any prior invocation of
     * SQLITE_CONFIG_MALLOC. If the memory pointer is not NULL then the alternative memory allocator
     * is engaged to handle all of SQLites memory allocation needs. The first pointer (the memory
     * pointer) must be aligned to an 8-byte boundary or subsequent behavior of SQLite will be
     * undefined. The minimum allocation size is capped at 2**12. Reasonable values for the minimum
     * allocation size are 2**5 through 2**8.
     */
    public class HEAP(
        internal val pMem: sqlite3_mutable_pointer?,
        internal val nBytes: Int,
        internal val min: Int
    ) : Sqlite3ConfigOption(8)

    /**
     * Enables or disables the collection of memory allocation statistics. When memory allocation
     * statistics are disabled, the following SQLite interfaces become non-operational:
     * - sqlite3_hard_heap_limit64()
     * - sqlite3_memory_used()
     * - sqlite3_memory_highwater()
     * - sqlite3_soft_heap_limit64()
     * - sqlite3_status64()
     *
     * Memory allocation statistics are enabled by default unless SQLite is compiled with
     * SQLITE_DEFAULT_MEMSTATUS=0 in which case memory allocation statistics are disabled by
     * default.
     */
    public class MEMSTATUS(internal val enabled: Int) : Sqlite3ConfigOption(9)

    /**
     * The SQLITE_CONFIG_LOOKASIDE option takes two arguments that determine the default size of
     * lookaside memory on each database connection. The first argument is the size of each
     * lookaside buffer slot ("[sz]") and the second is the number of slots allocated to each
     * database connection ("[cnt]"). SQLITE_CONFIG_LOOKASIDE sets the default lookaside size. The
     * SQLITE_DBCONFIG_LOOKASIDE option to sqlite3_db_config() can be used to change the lookaside
     * configuration on individual connections. The -DSQLITE_DEFAULT_LOOKASIDE option can be used to
     * change the default lookaside configuration at compile-time.
     */
    public class LOOKASIDE(
        internal val sz: Int,
        internal val cnt: Int
    ) : Sqlite3ConfigOption(13)

    /**
     * The SQLITE_CONFIG_LOG option is used to configure the SQLite global error log.
     * The SQLITE_CONFIG_LOG option takes two arguments: a pointer to a function with a call
     * signature of void(*)(void*,int,const char*), and a pointer to void. If the function pointer
     * is not NULL, it is invoked by sqlite3_log() to process each logging event. If the function
     * pointer is NULL, the sqlite3_log() interface becomes a no-op. The void pointer that is the
     * second argument to SQLITE_CONFIG_LOG is passed through as the first parameter to the
     * application-defined logger function whenever that function is invoked. The second parameter
     * to the logger function is a copy of the first parameter to the corresponding sqlite3_log()
     * call and is intended to be a result code or an extended result code. The third parameter
     * passed to the logger is a log message after formatting via sqlite3_snprintf(). The SQLite
     * logging interface is not reentrant; the logger function supplied by the application must not
     * invoke any SQLite interface. In a multi-threaded application, the application-defined logger
     * function must be threadsafe.
     */
    public class LOG(
        internal val callback: Sqlite3LogCallback?,
        internal val userData: sqlite3_mutable_pointer?
    ) : Sqlite3ConfigOption(16)

    /**
     * The SQLITE_CONFIG_URI option takes a single argument of type int. If non-zero, then URI
     * handling is globally enabled. If the parameter is zero, then URI handling is globally
     * disabled. If URI handling is globally enabled, all filenames passed to sqlite3_open(),
     * sqlite3_open_v2(), sqlite3_open16() or specified as part of ATTACH commands are interpreted
     * as URIs, regardless of whether or not the SQLITE_OPEN_URI flag is set when the database
     * connection is opened. If it is globally disabled, filenames are only interpreted as URIs if
     * the SQLITE_OPEN_URI flag is set when the database connection is opened. By default, URI
     * handling is globally disabled. The default value may be changed by compiling with the
     * SQLITE_USE_URI symbol defined.
     */
    public class URI(internal val value: Int) : Sqlite3ConfigOption(17)

    /**
     * The SQLITE_CONFIG_COVERING_INDEX_SCAN option takes a single integer argument which is 
     * interpreted as a boolean in order to enable or disable the use of covering indices for full 
     * table scans in the query optimizer. The default setting is determined by the 
     * SQLITE_ALLOW_COVERING_INDEX_SCAN compile-time option, or is "on" if that compile-time option 
     * is omitted. The ability to disable the use of covering indices for full table scans is 
     * because some incorrectly coded legacy applications might malfunction when the optimization 
     * is enabled. Providing the ability to disable the optimization allows the older, buggy 
     * application code to work without change even with newer versions of SQLite.
     */
    public class COVERING_INDEX_SCAN(internal val enabled: Int) : Sqlite3ConfigOption(20)

    /**
     * This option is only available if sqlite is compiled with the SQLITE_ENABLE_SQLLOG
     * pre-processor macro defined. The first argument should be a pointer to a function of type
     * void(*)(void*,sqlite3*,const char*, int). The second should be of type (void*). The callback
     * is invoked by the library in three separate circumstances, identified by the value passed as
     * the fourth parameter. If the fourth parameter is 0, then the database connection passed as
     * the second argument has just been opened. The third argument points to a buffer containing
     * the name of the main database file. If the fourth parameter is 1, then the SQL statement
     * that the third parameter points to has just been executed. Or, if the fourth parameter is 2,
     * then the connection being passed as the second parameter is being closed. The third parameter
     * is passed NULL In this case. An example of using this configuration option can be seen in the
     * "test_sqllog.c" source file in the canonical SQLite source tree.
     */
    public class SQLLOG(
        internal val callback: Sqlite3SqlLogCallback?,
        internal val userData: sqlite3_mutable_pointer?
    ) : Sqlite3ConfigOption(21)

    /**
     * SQLITE_CONFIG_MMAP_SIZE takes two 64-bit integer (sqlite3_int64) values that are the default
     * mmap size limit (the default setting for PRAGMA mmap_size) and the maximum allowed mmap size
     * limit. The default setting can be overridden by each database connection using either the
     * PRAGMA mmap_size command, or by using the SQLITE_FCNTL_MMAP_SIZE file control. The maximum
     * allowed mmap size will be silently truncated if necessary so that it does not exceed the
     * compile-time maximum mmap size set by the SQLITE_MAX_MMAP_SIZE compile-time option. If either
     * argument to this option is negative, then that argument is changed to its compile-time
     * default.
     */
    public class MMAP_SIZE(
        internal val sz: Long,
        internal val mx: Long
    ) : Sqlite3ConfigOption(22)

    /**
     * The SQLITE_CONFIG_WIN32_HEAPSIZE option is only available if SQLite is compiled for Windows
     * with the SQLITE_WIN32_MALLOC pre-processor macro defined. SQLITE_CONFIG_WIN32_HEAPSIZE takes
     * a 32-bit unsigned integer value that specifies the maximum size of the created heap.
     */
    public class WIN32_HEAPSIZE(internal val nByte: UInt) : Sqlite3ConfigOption(23)

    /**
     * The SQLITE_CONFIG_PMASZ option takes a single parameter which is an unsigned integer and sets
     * the "Minimum PMA Size" for the multithreaded sorter to that integer. The default minimum PMA
     * Size is set by the SQLITE_SORTER_PMASZ compile-time option. New threads are launched to help
     * with sort operations when multithreaded sorting is enabled (using the PRAGMA threads command)
     * and the amount of content to be sorted exceeds the page size times the minimum of the PRAGMA
     * cache_size setting and this value.
     */
    public class PMASZ(internal val szPma: UInt) : Sqlite3ConfigOption(25)

    /**
     * The SQLITE_CONFIG_STMTJRNL_SPILL option takes a single parameter which becomes the statement
     * journal spill-to-disk threshold. Statement journals are held in memory until their size
     * (in bytes) exceeds this threshold, at which point they are written to disk. Or if the
     * threshold is -1, statement journals are always held exclusively in memory. Since many
     * statement journals never become large, setting the spill threshold to a value such as 64KiB
     * can greatly reduce the amount of I/O required to support statement rollback. The default
     * value for this setting is controlled by the SQLITE_STMTJRNL_SPILL compile-time option.
     */
    public class STMTJRNL_SPILL(internal val nByte: Int) : Sqlite3ConfigOption(26)

    /**
     * Provides a hint to SQLite that it should avoid large memory  allocations if possible. SQLite
     * will run faster if it is free to make large memory allocations, but some applications might
     * prefer to run slower in exchange for guarantees about memory fragmentation that are possible
     * if large allocations are avoided. This hint is normally off.
     */
    public class SMALL_MALLOC(internal val enabled: Int) : Sqlite3ConfigOption(27)

    /**
     * The SQLITE_CONFIG_SORTERREF_SIZE option accepts a single parameter of type (int) - the new
     * value of the sorter-reference size threshold. Usually, when SQLite uses an external sort to
     * order records according to an ORDER BY clause, all fields required by the caller are present
     * in the sorted records. However, if SQLite determines based on the declared type of a table
     * column that its values are likely to be very large - larger than the configured
     * sorter-reference size threshold - then a reference is stored in each sorted record and the
     * required column values loaded from the database as records are returned in sorted order.
     * The default value for this option is to never use this optimization. Specifying a negative
     * value for this option restores the default behavior. This option is only available if SQLite
     * is compiled with the SQLITE_ENABLE_SORTER_REFERENCES compile-time option.
     */
    public class SORTERREF_SIZE(internal val nByte: Int) : Sqlite3ConfigOption(28)

    /**
     * The SQLITE_CONFIG_MEMDB_MAXSIZE option accepts a single parameter sqlite3_int64 parameter
     * which is the default maximum size for an in-memory database created
     * using sqlite3_deserialize(). This default maximum size can be adjusted up or down for
     * individual databases using the SQLITE_FCNTL_SIZE_LIMIT file-control. If this configuration
     * setting is never used, then the default maximum is determined by the
     * SQLITE_MEMDB_DEFAULT_MAXSIZE compile-time option. If that compile-time option is not set,
     * then the default maximum is `1073741824`.
     */
    public class MEMDB_MAXSIZE(internal val maxSize: Long) : Sqlite3ConfigOption(29)

    /**
     * The SQLITE_CONFIG_ROWID_IN_VIEW option enables or disables the ability for VIEWs to have a
     * ROWID. The capability can only be enabled if SQLite is compiled with
     * -DSQLITE_ALLOW_ROWID_IN_VIEW, in which case the capability defaults to on. This configuration
     * option queries the current setting or changes the setting to off or on. The argument is a
     * pointer to an integer. If that integer initially holds a value of 1, then the ability for
     * VIEWs to have ROWIDs is activated. If the integer initially holds zero, then the ability is
     * deactivated. Any other initial value for the integer leaves the setting unchanged. After
     * changes, if any, the integer is written with a 1 or 0, if the ability for VIEWs to have
     * ROWIDs is on or off. If SQLite is compiled without -DSQLITE_ALLOW_ROWID_IN_VIEW (which is the
     * usual and recommended case) then the integer is always filled with zero, regardless if its
     * initial value.
     */
    public class ROWID_IN_VIEW(internal val param: Sqlite3IntParam) : Sqlite3ConfigOption(30)
}