@file:Suppress("ClassName")

package ksqlite.types

/**
 * The [sqlite3_backup] object records state information about an ongoing online backup operation.
 * The [sqlite3_backup] object is created by a call to [ksqlite.sqlite3_backup_init] and is
 * destroyed by a call to [ksqlite.sqlite3_backup_finish].
 *
 * [sqlite3_backup](https://sqlite.org/c3ref/backup.html)
 */
public expect class sqlite3_backup

/**
 * An instance of this object represents an open BLOB on which incremental BLOB I/O can be
 * performed.
 * Objects of this type are created by [ksqlite.sqlite3_blob_open] and destroyed by
 * [ksqlite.sqlite3_blob_close].
 * The [ksqlite.sqlite3_blob_read] and [ksqlite.sqlite3_blob_write] interfaces can be used to read
 * or write small subsections of the BLOB.
 * The [ksqlite.sqlite3_blob_bytes] interface returns the size of the BLOB in bytes.
 *
 * [sqlite3_blob](https://sqlite.org/c3ref/blob.html)
 */
public expect class sqlite3_blob

/**
 * An instance of the snapshot object records the state of a WAL mode database for some specific
 * point in history.
 *
 * [sqlite3_snapshot](https://sqlite.org/c3ref/snapshot.html)
 */
public expect class sqlite3_snapshot