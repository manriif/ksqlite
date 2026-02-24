@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi

import ksqlite.capi.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.capi.types.Sqlite3BlobOpenFlag
import ksqlite.capi.types.Sqlite3BlobParam
import ksqlite.capi.types.Sqlite3CheckpointMode
import ksqlite.capi.types.Sqlite3DestructorCallback
import ksqlite.capi.types.Sqlite3IntParam
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3SnapshotParam
import ksqlite.capi.types.Sqlite3TextEncoding
import ksqlite.capi.types.Sqlite3WalCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_backup
import ksqlite.capi.types.sqlite3_blob
import ksqlite.capi.types.sqlite3_context
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_pointer
import ksqlite.capi.types.sqlite3_snapshot
import ksqlite.capi.types.sqlite3_stmt
import ksqlite.capi.types.sqlite3_value

/**
 * Register a function to be invoked prior to each autovacuum that determines the number of pages
 * to vacuum.
 *
 * [sqlite3_autovacuum_pages()](https://sqlite.org/c3ref/autovacuum_pages.html)
 */
public expect fun sqlite3_autovacuum_pages(
    db: sqlite3,
    userData: sqlite3_pointer?,
    destructor: Sqlite3DestructorCallback?,
    callback: Sqlite3AutoVacuumPagesCallback?
): Sqlite3Result

/**
 * Release all resources associated with an [sqlite3_backup]* handle.
 *
 * [sqlite3_backup_finish()](https://sqlite.org/c3ref/backup_finish.html#sqlite3backupfinish)
 */
public expect fun sqlite3_backup_finish(backup: sqlite3_backup): Sqlite3Result

/**
 * Create an [sqlite3_backup] process to copy the contents of [srcDbName] from connection handle
 * [srcDb] to [destDbName] in [destDb].
 * If successful, return a pointer to the new [sqlite3_backup] object.
 * If an error occurs, NULL is returned and an error code and error message stored in database
 * handle [destDb].
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
    outBlob: Sqlite3BlobParam
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
    outSnapshot: Sqlite3SnapshotParam
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

/**
 * Attempt to return the underlying operating system error code or error number that caused the most
 * recent I/O error or failure to open a file. The return value is OS-dependent.
 *
 * [sqlite3_system_errno()](https://sqlite.org/c3ref/system_errno.html)
 */
public expect fun sqlite3_system_errno(db: sqlite3): Int

/**
 * Return the current text encoding of the [value].
 *
 * [sqlite3_value_encoding()](https://sqlite.org/c3ref/value_encoding.html)
 */
public expect fun sqlite3_value_encoding(value: sqlite3_value): Sqlite3TextEncoding.Set2?

/**
 * Configure an [sqlite3_wal_hook] callback to automatically checkpoint a database after committing
 * a transaction if there are [nFrame] or more frames in the log file. Passing zero or a negative
 * value as the [nFrame] parameter disables automatic checkpoints entirely.
 *
 * The callback registered by this function replaces any existing callback egistered using
 * [sqlite3_wal_hook]. Likewise, registering a callback using [sqlite3_wal_hook] disables the
 * automatic checkpoint mechanism configured by this function.
 *
 * [sqlite3_wal_autocheckpoint()](https://sqlite.org/c3ref/wal_autocheckpoint.html)
 */
public expect fun sqlite3_wal_autocheckpoint(
    db: sqlite3,
    nFrame: Int
): Sqlite3Result

/**
 * Checkpoint database [name]. If [name] is NULL, or if the buffer [name] points to contains a
 * zero-length string, all attached databases are checkpointed.
 *
 * [sqlite3_wal_checkpoint()](https://sqlite.org/c3ref/wal_checkpoint.html)
 */
public expect fun sqlite3_wal_checkpoint(
    db: sqlite3,
    name: String?
): Sqlite3Result

/**
 * Checkpoint database [name]. If [name] is NULL, or if the buffer [name] points to contains a
 * zero-length string, all attached databases are checkpointed.
 *
 * [sqlite3_wal_checkpoint_v2()](https://sqlite.org/c3ref/wal_checkpoint_v2.html)
 */
public expect fun sqlite3_wal_checkpoint_v2(
    db: sqlite3,
    name: String?,
    mode: Sqlite3CheckpointMode,
    outNLog: Sqlite3IntParam?,
    outNCkpt: Sqlite3IntParam?
): Sqlite3Result

/**
 * Register a callback to be invoked each time a transaction is written into the write-ahead-log by
 * this database connection.
 *
 * [sqlite3_wal_hook()](https://sqlite.org/c3ref/wal_hook.html)
 */
public expect fun sqlite3_wal_hook(
    sqlite3: sqlite3,
    userData: sqlite3_pointer?,
    callback: Sqlite3WalCallback?
): sqlite3_mutable_pointer?