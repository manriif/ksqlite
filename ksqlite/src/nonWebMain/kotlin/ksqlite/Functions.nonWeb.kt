@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite

import ksqlite.types.Sqlite3AutoVacuumPagesCallback
import ksqlite.types.Sqlite3DestructorCallback
import ksqlite.types.Sqlite3Result
import ksqlite.types.Sqlite3TextEncoding
import ksqlite.types.pointer
import ksqlite.types.sqlite3
import ksqlite.types.sqlite3_backup
import ksqlite.types.sqlite3_blob
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
public expect fun sqlite3_backup_pagecount(
    backup: sqlite3_backup
): Int

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
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_blob64(
    stmt: sqlite3_stmt,
    index: Int,
    data: pointer,
    nData: ULong
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_text64(
    stmt: sqlite3_stmt,
    index: Int,
    data: String?,
    nData: ULong,
    encoding: Sqlite3TextEncoding.Set1
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_value(
    stmt: sqlite3_stmt,
    index: Int,
    value: sqlite3_value
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_zeroblob(
    stmt: sqlite3_stmt,
    index: Int,
    n: Int
): Sqlite3Result

/**
 * Bind a blob value to an SQL statement variable.
 *
 * [sqlite3_bind_blob()](https://sqlite.org/c3ref/bind_blob.html)
 */
public expect fun sqlite3_bind_zeroblob64(
    stmt: sqlite3_stmt,
    index: Int,
    n: ULong
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
    flags: Int,
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