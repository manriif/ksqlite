package ksqlite

///////////////////////////////////////////////////////////////////////////
// SQLite
///////////////////////////////////////////////////////////////////////////

/**
 * The [sqlite3_backup] object records state information about an ongoing online backup operation.
 * The [sqlite3_backup] object is created by a call to [sqlite3_backup_init] and is destroyed by a
 * call to [sqlite3_backup_finish].
 *
 * [sqlite3_backup](https://sqlite.org/c3ref/backup.html)
 */
public expect class sqlite3_backup

/**
 * An instance of this object represents an open BLOB on which incremental BLOB I/O can be
 * performed.
 * Objects of this type are created by [sqlite3_blob_open] and destroyed by [sqlite3_blob_close].
 * The [sqlite3_blob_read] and [sqlite3_blob_write] interfaces can be used to read or write small
 * subsections of the BLOB.
 * The [sqlite3_blob_bytes] interface returns the size of the BLOB in bytes.
 *
 * [sqlite3_blob](https://sqlite.org/c3ref/blob.html)
 */
public expect class sqlite3_blob

///////////////////////////////////////////////////////////////////////////
// Callbacks
///////////////////////////////////////////////////////////////////////////

/**
 * Callback for [sqlite3_autovacuum_pages].
 */
public typealias AutoVacuumPagesCallback = (
    zSchema: String,
    nDbPage: UInt,
    nFreePage: UInt,
    nBytePerPage: UInt
) -> UInt