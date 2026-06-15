@file:Suppress("SpellCheckingInspection", "ClassName")

package ksqlite.types

/**
 * These bit values are intended for use in the 3rd parameter to the sqlite3_open_v2() interface and
 * in the 4th parameter to the sqlite3_vfs.xOpen method.
 *
 * [Flags For File Open Operations](https://sqlite.org/c3ref/c_open_autoproxy.html)
 * [Opening A New Database Connection](https://sqlite.org/c3ref/open.html)
 * [sqlite3_vfs](https://sqlite.org/c3ref/vfs.html)
 */
public sealed class SqliteOpenFlag(public open val value: Int) {

    ///////////////////////////////////////////////////////////////////////////
    // Common DB + VFS
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Flag or ORed flags for use with [ksqlite.capi.sqlite3_open_v2] only.
     */
    public sealed class Db(value: Int) : SqliteOpenFlag(value) {

        /**
         * Returns a [Vfs] which could be used to add optional flags for VFS.
         */
        public fun vfs(): Vfs = Vfs.Mask(value)

        /**
         * Returns a [Db] which is ORed with [flag].
         */
        public open infix fun or(flag: OptionalDb): Db = Mask(value or flag.value)

        /**
         * Holder for flags.
         */
        @ConsistentCopyVisibility
        public data class Mask internal constructor(override val value: Int) : Db(value)
    }

    /**
     * Flag required to open a database connection.
     */
    public sealed class Required(value: Int) : Db(value)

    /**
     * Optional flag for use with [ksqlite.capi.sqlite3_open_v2] and VFS.
     */
    public sealed class Optional(value: Int) : SqliteOpenFlag(value)

    /**
     * Optional flag for use with [ksqlite.capi.sqlite3_open_v2] only.
     */
    public sealed class OptionalDb(value: Int) : Optional(value)

    /**
     * The database is opened in read-only mode. If the database does not already exist, an error is
     * returned.
     */
    public data object READONLY : Required(0x00000001)

    /**
     * The database is opened for reading and writing if possible, or reading only if the file is
     * write protected by the operating system. In either case the database must already exist,
     * otherwise an error is returned. For historical reasons, if opening in read-write mode fails
     * due to OS-level permissions, an attempt is made to open it in read-only mode.
     * sqlite3_db_readonly() can be used to determine whether the database is actually read-write.
     */
    public data object READWRITE : Required(0x00000002) {

        /**
         * Returns a [Db] which is ORed with [flag].
         */
        public infix fun or(flag: CREATE): Db = Mask(value or flag.value)
    }

    /**
     * The database is opened for reading and writing, and is created if it does not already exist.
     * This is the behavior that is always used for sqlite3_open() and sqlite3_open16().
     */
    public data object CREATE : SqliteOpenFlag(0x00000004) {

        /**
         * Returns a [Db] which is ORed with [flag].
         */
        public infix fun or(flag: READWRITE): Db = flag or this
    }

    /**
     * The filename can be interpreted as a URI if this flag is set.
     */
    public data object URI : OptionalDb(0x00000040)

    /**
     * The database will be opened as an in-memory database. The database is named by the "filename"
     * argument for the purposes of cache-sharing, if shared cache mode is enabled, but the
     * "filename" is otherwise ignored.
     */
    public data object MEMORY : OptionalDb(0x00000080)

    /**
     * The new database connection will use the "multi-thread" threading mode. This means that
     * separate threads are allowed to use SQLite at the same time, as long as each thread is using
     * a different database connection.
     */
    public data object NOMUTEX : OptionalDb(0x00008000)

    /**
     * The new database connection will use the "serialized" threading mode. This means the multiple
     * threads can safely attempt to use the same database connection at the same time. (Mutexes
     * will block any actual concurrency, but in this mode there is no harm in trying.)
     */
    public data object FULLMUTEX : OptionalDb(0x00010000)

    /**
     * The database is opened with shared cache enabled, overriding the default shared cache setting
     * provided by sqlite3_enable_shared_cache(). The use of shared cache mode is discouraged and
     * hence shared cache capabilities may be omitted from many builds of SQLite. In such cases,
     * this option is a no-op.
     */
    @Deprecated("The use of shared cache mode is discouraged")
    public data object SHAREDCACHE : OptionalDb(0x00020000)

    /**
     * The database is opened with shared cache disabled, overriding the default shared cache
     * setting provided by sqlite3_enable_shared_cache().
     */
    public data object PRIVATECACHE : OptionalDb(0x00040000)

    /**
     * The database connection comes up in "extended result code mode". In other words, the database
     * behaves as if sqlite3_extended_result_codes(db,1) were called on the database connection as
     * soon as the connection is created. In addition to setting the extended result code mode, this
     * flag also causes sqlite3_open_v2() to return an extended result code.
     */
    public data object NOFOLLOW : OptionalDb(0x01000000)

    /**
     * The database filename is not allowed to contain a symbolic link.
     */
    public data object EXRESCODE : OptionalDb(0x02000000)

    ///////////////////////////////////////////////////////////////////////////
    // VFS only
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Flag or ORed flags that meet SQLite requirements for opening a file in VFS context.
     */
    public sealed class Vfs(value: Int) : SqliteOpenFlag(value) {

        /**
         * Returns a [Vfs] which is ORed with [flag].
         */
        public infix fun or(flag: Optional): Vfs = Mask(value or flag.value)

        /**
         * Holder for VFS only flags.
         */
        @ConsistentCopyVisibility
        public data class Mask internal constructor(override val value: Int) : Vfs(value)
    }

    /**
     * Optional flag availaible for VFS.
     */
    public sealed class OptionalVfs(value: Int) : Optional(value)

    /**
     * The SQLITE_OPEN_DELETEONCLOSE flag means the file should be deleted when it is closed. The
     * SQLITE_OPEN_DELETEONCLOSE will be set for TEMP databases and their journals, transient
     * databases, and subjournals.
     */
    public data object DELETEONCLOSE : OptionalVfs(0x00000008)

    /**
     * The SQLITE_OPEN_EXCLUSIVE flag is always used in conjunction with the SQLITE_OPEN_CREATE
     * flag, which are both directly analogous to the O_EXCL and O_CREAT flags of the POSIX open()
     * API. The SQLITE_OPEN_EXCLUSIVE flag, when paired with the SQLITE_OPEN_CREATE, is used to
     * indicate that file should always be created, and that it is an error if it already exists.
     * It is not used to indicate the file should be opened for exclusive access.
     */
    public data object EXCLUSIVE : OptionalVfs(0x00000010)

    public data object AUTOPROXY : OptionalVfs(0x00000020)

    public data object MAIN_DB : OptionalVfs(0x00000100)

    public data object TEMP_DB : OptionalVfs(0x00000200)

    public data object TRANSIENT_DB : OptionalVfs(0x00000400)

    public data object MAIN_JOURNAL : OptionalVfs(0x00000800)

    public data object TEMP_JOURNAL : OptionalVfs(0x00001000)

    public data object SUBJOURNAL : OptionalVfs(0x00002000)

    public data object SUPER_JOURNAL : OptionalVfs(0x00004000)

    public data object WAL : OptionalVfs(0x00080000)
}