@file:Suppress("FunctionName", "SpellCheckingInspection")

package ksqlite.capi
/*
import ksqlite.capi.types.Int32OutputParam
import ksqlite.capi.types.Sqlite3CheckpointMode
import ksqlite.capi.types.Sqlite3Result
import ksqlite.capi.types.Sqlite3SnapshotOutputParam
import ksqlite.capi.types.Sqlite3WalHookCallback
import ksqlite.capi.types.sqlite3
import ksqlite.capi.types.sqlite3_mutable_pointer
import ksqlite.capi.types.sqlite3_snapshot

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
public expect fun sqlite3_snapshot_free(snapshot: sqlite3_snapshot)

/**
 * Obtain a snapshot handle for the snapshot of database zDb currently being read by handle db.
 *
 * [sqlite3_snapshot_get()](https://sqlite.org/c3ref/snapshot_get.html)
 */
public expect fun sqlite3_snapshot_get(
    db: sqlite3,
    name: String?,
    outSnapshot: Sqlite3SnapshotOutputParam
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
    outNLog: Int32OutputParam?,
    outNCkpt: Int32OutputParam?
): Sqlite3Result

/**
 * Register a callback to be invoked each time a transaction is written into the write-ahead-log by
 * this database connection.
 *
 * [sqlite3_wal_hook()](https://sqlite.org/c3ref/wal_hook.html)
 */
public expect fun sqlite3_wal_hook(
    db: sqlite3,
    userData: sqlite3_mutable_pointer?,
    callback: Sqlite3WalHookCallback?
): sqlite3_mutable_pointer?*/