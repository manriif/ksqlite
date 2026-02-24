package ksqlite.capi.types

/**
 * These constants define all valid values for the "checkpoint mode" passed as the third parameter
 * to the sqlite3_wal_checkpoint_v2() interface. See the sqlite3_wal_checkpoint_v2() documentation
 * for details on the meaning of each of these checkpoint modes.
 *
 * [Checkpoint Mode Values](https://sqlite.org/c3ref/c_checkpoint_full.html)
 */
public enum class Sqlite3CheckpointMode(internal val id: Int) {

	/**
	 * This mode always checkpoints zero frames. The only reason to invoke a NOOP checkpoint is to
	 * access the values returned by sqlite3_wal_checkpoint_v2() via output parameters *pnLog and
	 * *pnCkpt.
	 */
	NOOP(-1),

	/**
	 * Checkpoint as many frames as possible without waiting for any database readers or writers to
	 * finish, then sync the database file if all frames in the log were checkpointed. The
	 * busy-handler callback is never invoked in the SQLITE_CHECKPOINT_PASSIVE mode. On the other
	 * hand, passive mode might leave the checkpoint unfinished if there are concurrent readers or
	 * writers.
	 */
	PASSIVE(0),  /* Do as much as possible w/o blocking */

	/**
	 * This mode blocks (it invokes the busy-handler callback) until there is no database writer and
	 * all readers are reading from the most recent database snapshot. It then checkpoints all
	 * frames in the log file and syncs the database file. This mode blocks new database writers
	 * while it is pending, but new database readers are allowed to continue unimpeded.
	 */
	FULL(1),  /* Wait for writers, then checkpoint */

	/**
	 * This mode works the same way as SQLITE_CHECKPOINT_FULL with the addition that after
	 * checkpointing the log file it blocks (calls the busy-handler callback) until all readers are
	 * reading from the database file only. This ensures that the next writer will restart the log
	 * file from the beginning. Like SQLITE_CHECKPOINT_FULL, this mode blocks new database writer
	 * attempts while it is pending, but does not impede readers.
	 */
	RESTART(2),  /* Like FULL but wait for readers */

	/**
	 * This mode works the same way as SQLITE_CHECKPOINT_RESTART with the addition that it also
	 * truncates the log file to zero bytes just prior to a successful return.
	 */
	TRUNCATE(3),
}