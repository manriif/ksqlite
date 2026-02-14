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

///////////////////////////////////////////////////////////////////////////
// Callbacks
///////////////////////////////////////////////////////////////////////////

/**
 * Callback used in [sqlite3_autovacuum_pages].
 */
public typealias AutoVacuumPagesCallback<Data> = (
    pArg: Data?,
    zSchema: String,
    nDbPage: UInt,
    nFreePage: UInt,
    nBytePerPage: UInt
) -> UInt

///////////////////////////////////////////////////////////////////////////
// Internal
///////////////////////////////////////////////////////////////////////////

/**
 * Wrapper for [sqlite3_autovacuum_pages] parameters that can be passed as user_data pointer.
 */
internal class AutovacuumPages<Data>(
    val db: sqlite3,
    val data: Data?,
    val callback: AutoVacuumPagesCallback<Data>?
)