@file:Suppress("ClassName", "SpellCheckingInspection")

package ksqlite.capi.types

import ksqlite.capi.memory.Buffer

/**
 * These integer constants are opcodes for the xFileControl method of the sqlite3_io_methods object
 * and for the sqlite3_file_control() interface.
 *
 * [Standard File Control Opcodes](https://sqlite.org/c3ref/c_fcntl_begin_atomic_write.html)
 */
public sealed class SqliteFileControlOpcode(public open val code: Int) {

    /**
     * [SqliteFileControlOpcode] where the fourth argument is a pointer to a 32-bits integer.
     */
    public sealed class IntParam(
        code: Int,
        internal val param: Int32OutputParam
    ): SqliteFileControlOpcode(code)

    /**
     * [SqliteFileControlOpcode] where the fourth argument is a pointer to a 64-bits integer.
     */
    public sealed class LongParam(
        code: Int,
        internal val param: Int64OutputParam
    ): SqliteFileControlOpcode(code)

    /**
     * [SqliteFileControlOpcode] where the fourth argument is a pointer to a string.
     */
    public sealed class StringParam(
        code: Int,
        internal val param: Utf8OutputParam
    ): SqliteFileControlOpcode(code)

    /**
     * Writes the platform specific error code into [param].
     */
    public class LAST_ERRNO(param: Int32OutputParam) : IntParam(4, param)

    /**
     * The SQLITE_FCNTL_SIZE_HINT opcode is used by SQLite to give the VFS layer a hint of how large
     * the database file will grow to be during the current transaction. This hint is not guaranteed
     * to be accurate but it is often close. The underlying VFS might choose to preallocate database
     * file space based on this hint in order to help writes to the database file run faster.
     */
    public class SIZE_HINT(param: Int64OutputParam) : LongParam(5, param)

    /**
     * The SQLITE_FCNTL_CHUNK_SIZE opcode is used to request that the VFS extends and truncates the
     * database file in chunks of a size specified by the user. The fourth argument to
     * sqlite3_file_control() should point to an integer (type int) containing the new chunk-size to
     * use for the nominated database. Allocating database file space in large chunks (say 1MB at a
     * time), may reduce file-system fragmentation and improve performance on some systems.
     */
    public class CHUNK_SIZE(param: Int32OutputParam) : IntParam(6, param)

    /**
     * The SQLITE_FCNTL_PERSIST_WAL opcode is used to set or query the persistent Write Ahead Log
     * setting. By default, the auxiliary write ahead log (WAL file) and shared memory files used
     * for transaction control are automatically deleted when the latest connection to the database
     * closes. Setting persistent WAL mode causes those files to persist after close. Persisting the
     * files is useful when other processes that do not have write permission on the directory
     * containing the database file want to read the database file, as the WAL and shared memory
     * files must exist in order for the database to be readable. The fourth parameter to
     * sqlite3_file_control() for this opcode should be a pointer to an integer. That integer is 0
     * to disable persistent WAL mode or 1 to enable persistent WAL mode. If the integer is -1, then
     * it is overwritten with the current WAL persistence setting.
     */
    public class PERSIST_WAL(param: Int32OutputParam) : IntParam(10, param)

    /**
     * The SQLITE_FCNTL_OVERWRITE opcode is invoked by SQLite after opening a write transaction to
     * indicate that, unless it is rolled back for some reason, the entire database file will be
     * overwritten by the current transaction. This is used by VACUUM operations.
     */
    public class OVERWRITE(param: Int64OutputParam) : LongParam(11, param)

    /**
     * The SQLITE_FCNTL_VFSNAME opcode can be used to obtain the names of all VFSes in the VFS
     * stack. The names of all VFS shims and the final bottom-level VFS are written into memory
     * obtained from sqlite3_malloc() and the result is stored in the char* variable that the fourth
     * parameter of sqlite3_file_control() points to. The caller is responsible for freeing the
     * memory when done. As with all file-control actions, there is no guarantee that this will
     * actually do anything. Callers should initialize the char* variable to a NULL pointer in case
     * this file-control is not implemented. This file-control is intended for diagnostic use only.
     */
    public class VFSNAME(param: Utf8OutputParam) : StringParam(12, param)

    /**
     * The SQLITE_FCNTL_POWERSAFE_OVERWRITE opcode is used to set or query the persistent
     * "powersafe-overwrite" or "PSOW" setting. The PSOW setting determines the
     * SQLITE_IOCAP_POWERSAFE_OVERWRITE bit of the xDeviceCharacteristics methods. The fourth
     * parameter to sqlite3_file_control() for this opcode should be a pointer to an integer. That
     * integer is 0 to disable zero-damage mode or 1 to enable zero-damage mode. If the integer is
     * -1, then it is overwritten with the current zero-damage mode setting.
     */
    public class POWERSAFE_OVERWRITE(param: Int32OutputParam) : IntParam(13, param)

    /**
     * Applications can invoke the SQLITE_FCNTL_TEMPFILENAME file-control to have SQLite generate a
     * temporary filename using the same algorithm that is followed to generate temporary filenames
     * for TEMP tables and other internal uses. The argument should be a char** which will be filled
     * with the filename written into memory obtained from sqlite3_malloc(). The caller should
     * invoke sqlite3_free() on the result to avoid a memory leak.
     */
    public class TEMPFILENAME(param: Utf8OutputParam) : StringParam(16, param)

    /**
     * The SQLITE_FCNTL_MMAP_SIZE file control is used to query or set the maximum number of bytes
     * that will be used for memory-mapped I/O. The argument is a pointer to a value of type
     * sqlite3_int64 that is an advisory maximum number of bytes in the file to memory map. The
     * pointer is overwritten with the old value. The limit is not changed if the value originally
     * pointed to is negative, and so the current limit can be queried by passing in a pointer to a
     * negative number. This file-control is used internally to implement PRAGMA mmap_size.
     */
    public class MMAP_SIZE(param: Int64OutputParam) : LongParam(18, param)

    /**
     * The SQLITE_FCNTL_HAS_MOVED file control interprets its argument as a pointer to an integer
     * and it writes a boolean into that integer depending on whether or not the file has been
     * renamed, moved, or deleted since it was first opened.
     */
    public class HAS_MOVED(param: Int32OutputParam): IntParam(20, param)

    /**
     * The SQLITE_FCNTL_VFS_POINTER opcode finds a pointer to the top-level VFSes currently in use.
     * The argument X in sqlite3_file_control(db,SQLITE_FCNTL_VFS_POINTER,X) must be of type
     * "sqlite3_vfs **". This opcode will set *X to a pointer to the top-level VFS. When there are
     * multiple VFS shims in the stack, this opcode finds the upper-most shim only.
     */
    public class VFS_POINTER(internal val param: SqliteVfsOutputParam) : SqliteFileControlOpcode(27)

    /**
     * The SQLITE_FCNTL_LOCK_TIMEOUT opcode is used to configure a VFS to block for up to M
     * milliseconds before failing when attempting to obtain a file lock using the xLock or xShmLock
     * methods of the VFS. The parameter is a pointer to a 32-bit signed integer that contains the
     * value that M is to be set to. Before returning, the 32-bit signed integer is overwritten with
     * the previous value of M.
     */
    public class LOCK_TIMEOUT(param: Int32OutputParam) : IntParam(34, param)

    /**
     * The SQLITE_FCNTL_DATA_VERSION opcode is used to detect changes to a database file. The
     * argument is a pointer to a 32-bit unsigned integer. The "data version" for the pager is
     * written into the pointer. The "data version" changes whenever any change occurs to the
     * corresponding database file, either through SQL statements on the same database connection or
     * through transactions committed by separate database connections possibly in other processes.
     * The sqlite3_total_changes() interface can be used to find if any database on the connection
     * has changed, but that interface responds to changes on TEMP as well as MAIN and does not
     * provide a mechanism to detect changes to MAIN only. Also, the sqlite3_total_changes()
     * interface responds to internal changes only and omits changes made by other database
     * connections. The PRAGMA data_version command provides a mechanism to detect changes to a
     * single attached database that occur due to other database connections, but omits changes
     * implemented by the database connection on which it is called. This file control is the only
     * mechanism to detect changes that happen either internally or externally and that are
     * associated with a particular attached database.
     */
    public class DATA_VERSION(param: Int32OutputParam) : IntParam(35, param)

    /**
     * The SQLITE_FCNTL_SIZE_LIMIT opcode is used by in-memory VFS that implements
     * sqlite3_deserialize() to set an upper bound on the size of the in-memory database. The
     * argument is a pointer to a sqlite3_int64. If the integer pointed to is negative, then it is
     * filled in with the current limit. Otherwise the limit is set to the larger of the value of
     * the integer pointed to and the current database size. The integer pointed to is set to the
     * new limit.
     */
    public class SIZE_LIMIT(param: Int64OutputParam) : LongParam(36, param)

    public class RESERVE_BYTES(param: Int32OutputParam) : IntParam(38, param)

    /**
     * If there is currently no transaction open on the database, and the database is not a temp db,
     * then the SQLITE_FCNTL_RESET_CACHE file-control purges the contents of the in-memory page
     * cache. If there is an open transaction, or if the db is a temp-db, this opcode is a no-op,
     * not an error.
     */
    public object RESET_CACHE : SqliteFileControlOpcode(42)

    /**
     * Custom opcode.
     */
    public data class Custom(
        override val code: Int,
        val buffer: Buffer?
    ) : SqliteFileControlOpcode(code)
}