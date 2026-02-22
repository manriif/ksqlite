@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite

import ksqlite.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.types.Sqlite3BlobOpenFlag
import ksqlite.types.Sqlite3DestructorCallback
import ksqlite.types.Sqlite3PointerParam
import ksqlite.types.Sqlite3Result
import ksqlite.types.Sqlite3TextEncoding
import ksqlite.types.sqlite3
import ksqlite.types.sqlite3_backup
import ksqlite.types.sqlite3_blob
import ksqlite.types.sqlite3_context
import ksqlite.types.sqlite3_pointer
import ksqlite.types.sqlite3_snapshot
import ksqlite.types.sqlite3_stmt
import ksqlite.types.sqlite3_value

/**
 * Register a function to be invoked prior to each autovacuum that determines the number of pages
 * to vacuum.
 *
 * [sqlite3_autovacuum_pages()](https://sqlite.org/c3ref/autovacuum_pages.html)
 */
public expect fun sqlite3_autovacuum_pages(
    db: sqlite3,
    callback: Sqlite3AutoVacuumPagesCallback?,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result

/**
 * Release all resources associated with an [ksqlite.types.sqlite3_backup]* handle.
 *
 * [sqlite3_backup_finish()](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupfinish)
 */
public expect fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result

/**
 * Create an [sqlite3_backup] process to copy the contents of [srcDbName] from connection handle
 * [srcDb] to [destDbName] in [destDb].
 * If successful, return a pointer to the new [sqlite3_backup] object.
 * If an error occurs, NULL is returned and an error code and error message stored in database
 * handle pDestDb.
 *
 * [sqlite3_backup_init()](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupinit)
 */
public expect fun sqlite3_backup_init(
    destDb: sqlite3,
    destDbName: String,
    srcDb: sqlite3,
    srcDbName: String
): sqlite3_backup?

/**
 * Return the total number of pages in the source database as of the most recent call to
 * [sqlite3_backup_step].
 *
 * [sqlite3_backup_pagecount()](https://sqlite.org/c3ref/backup_finish.html#sqlite3backuppagecount)
 */
public expect fun sqlite3_backup_pagecount(backup: sqlite3_backup): Int

/**
 * Return the number of pages still to be backed up as of the most recent call to
 * [sqlite3_backup_step].
 *
 * [sqlite3_backup_remaining](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupremaining)
 */
public expect fun sqlite3_backup_remaining(backup: sqlite3_backup): Int

/**
 * Copy nPage pages from the source b-tree to the destination.
 *
 * [sqlite3_backup_step](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupstep)
 */
public expect fun sqlite3_backup_step(
    backup: sqlite3_backup,
    nPage: Int
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob64()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_pointer?,
    size: Long,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_text64()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    data: sqlite3_pointer?,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destructor: Sqlite3DestructorCallback?
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_value()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_zeroblob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    size: Int
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_zeroblob64()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    size: ULong
): Sqlite3Result

/**
 * Query a blob handle for the size of the data.
 *
 * [sqlite3_blob_bytes()](https://sqlite.org/c3ref/blob_bytes.html)
 */
public expect fun sqlite3_blob_bytes(blob: sqlite3_blob): Int

/**
 * Close a blob handle that was previously created using [sqlite3_blob_open].
 *
 * [sqlite3_blob_close()](https://sqlite.org/c3ref/blob_close.html)
 */
public expect fun sqlite3_blob_close(blob: sqlite3_blob): Sqlite3Result

/**
 * Open a blob handle.
 *
 * [sqlite3_blob_open()](https://sqlite.org/c3ref/blob_open.html)
 */
public expect fun sqlite3_blob_open(
    db: sqlite3,
    databaseName: String,
    tableName: String,
    columnName: String,
    rowIndex: Long,
    flags: Sqlite3BlobOpenFlag,
    blob: sqlite3_blob,
): Sqlite3Result

/**
 * Read data from a blob handle.
 *
 * [sqlite3_blob_read()](https://sqlite.org/c3ref/blob_read.html)
 */
public expect fun sqlite3_blob_read(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result

/**
 * Move an existing blob handle to point to a different row of the same database table.
 *
 * If an error occurs, or if the specified row does not exist or does not contain a blob or text
 * value, then an error code is returned and the database handle error code and message set.
 * If this happens, then all subsequent calls to sqlite3_blob_xxx() functions (except blob_close())
 * immediately return SQLITE_ABORT.
 *
 * [sqlite3_blob_reopen()](https://sqlite.org/c3ref/blob_reopen.html)
 */
public expect fun sqlite3_blob_reopen(
    blob: sqlite3_blob,
    rowIndex: Long
): Sqlite3Result

/**
 * Write data to a blob handle.
 *
 * [sqlite3_blob_write()](https://sqlite.org/c3ref/blob_write.html)
 */
public expect fun sqlite3_blob_write(
    blob: sqlite3_blob,
    buffer: ByteArray,
    size: Int,
    offset: Int
): Sqlite3Result

/**
 * Flush any dirty pages in the pager-cache for any attached database to disk.
 *
 * [sqlite3_db_cacheflush()](https://sqlite.org/c3ref/db_cacheflush.html)
 */
public expect fun sqlite3_db_cacheflush(db: sqlite3): Sqlite3Result

/**
 * Free up as much memory as we can from the given database connection.
 *
 * [sqlite3_db_release_memory()](https://sqlite.org/c3ref/db_release_memory.html)
 */
public expect fun sqlite3_db_release_memory(db: sqlite3): Sqlite3Result

/**
 * Set the hard heap-size limit for the library. An argument of zero disables the hard heap limit.
 * A negative argument is a no-op used to obtain the return value without affecting the hard heap
 * limit.
 *
 * The return value is the value of the hard heap limit just prior to calling this interface.
 *
 * Setting the hard heap limit will also activate the soft heap limit and constrain the soft heap
 * limit to be no more than the hard heap limit.
 *
 * [sqlite3_hard_heap_limit64()](https://sqlite.org/c3ref/hard_heap_limit64.html)
 */
public expect fun sqlite3_hard_heap_limit64(limit: Long): Long

/**
 * Write a [message] to the log if logging is enabled.
 *
 * [sqlite3_log()](https://sqlite.org/c3ref/log.html)
 */
public expect fun sqlite3_log(
    errCode: Int,
    message: String
)

/**
 * Return the amount of memory currently checked out.
 *
 * [sqlite3_memory_used()](https://sqlite.org/c3ref/memory_highwater.html)
 */
public expect fun sqlite3_memory_used(): Long

/**
 * Return the maximum amount of memory that has ever been checked out since either the beginning of
 * this process or since the most recent reset.
 *
 * [sqlite3_memory_highwater()](https://sqlite.org/c3ref/memory_highwater.html)
 */
public expect fun sqlite3_memory_highwater(): Long

/**
 * Return the normalized SQL associated with a prepared statement.
 *
 * [sqlite3_normalized_sql()](https://sqlite.org/c3ref/expanded_sql.html)
 */
public expect fun sqlite3_normalized_sql(stmt: sqlite3_stmt): String

/**
 * Attempt to release up to [size] bytes of non-essential memory currently held by SQLite. An
 * example of non-essential memory is memory used to cache database pages that are not currently in
 * use.
 *
 * [sqlite3_release_memory()](https://sqlite.org/c3ref/release_memory.html)
 */
public expect fun sqlite3_release_memory(size: Int): Int

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_blob64()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_blob64(
    context: sqlite3_context,
    data: sqlite3_pointer?,
    size: Long,
    destructor: Sqlite3DestructorCallback?
)

/**
 * Routine used by user-defined functions to specify the function result.
 *
 * [sqlite3_result_text64()](https://sqlite.org/c3ref/result_blob.html)
 */
public expect fun sqlite3_result_text64(
    context: sqlite3_context,
    data: sqlite3_pointer?,
    size: Long,
    encoding: Sqlite3TextEncoding.Set1,
    destructor: Sqlite3DestructorCallback?
)

/**
 * Return a +ve value if snapshot [snapshot1] is newer than [snapshot2]. A -ve value if [snapshot1]
 * is older than [snapshot2] and zero if [snapshot1] and [snapshot2] are the same snapshot.
 *
 * [sqlite3_snapshot_cmp()](https://sqlite.org/c3ref/snapshot_cmp.html)
 */
public expect fun sqlite3_snapshot_cmp(
    snapshot1: sqlite3_snapshot,
    snapshot2: sqlite3_snapshot
): Int

/**
 * Free a snapshot handle obtained from [sqlite3_snapshot_get].
 *
 * [sqlite3_snapshot_free()](https://sqlite.org/c3ref/snapshot_free.html)
 */
public expect fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot): Int

/**
 * Obtain a snapshot handle for the snapshot of database zDb currently being read by handle db.
 *
 * [sqlite3_snapshot_get()](https://sqlite.org/c3ref/snapshot_get.html)
 */
public expect fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    snapshot: Sqlite3PointerParam<sqlite3_snapshot>
): Sqlite3Result

/**
 * Open a read-transaction on the snapshot identified by [snapshot].
 *
 * [sqlite3_snapshot_open()](https://sqlite.org/c3ref/snapshot_open.html)
 */
public expect fun sqlite3_snapshot_open(
    db: sqlite3,
    name: String?,
    snapshot: sqlite3_snapshot
): Sqlite3Result

/**
 * Recover as many snapshots as possible from the wal file associated with schema zDb of database
 * [db].
 *
 * [sqlite3_snapshot_recover()](https://sqlite.org/c3ref/snapshot_recover.html)
 */
public expect fun sqlite3_snapshot_recover(
    db: sqlite3,
    name: String?
): Sqlite3Result

/**
 * Set the soft heap-size limit for the library. An argument of zero disables the limit. A negative
 * argument is a no-op used to obtain the return value.
 *
 * The return value is the value of the heap limit just before this interface was called.
 *
 * If the hard heap limit is enabled, then the soft heap limit cannot  be disabled nor raised above
 * the hard heap limit.
 *
 * [sqlite3_soft_heap_limit64()](https://sqlite.org/c3ref/hard_heap_limit64.html)
 */
public expect fun sqlite3_soft_heap_limit64(limit: Long): Long