package ksqlite.kapi.database

/**
 * Result of a Write-Ahead Log checkpoint.
 */
public interface WriteAheadLogCheckpointResult {

    /**
     * Total number of frames in the log file or to -1 if the checkpoint could not run because of
     * an error or because the database is not in WAL mode.
     */
    public val frameCount: Int

    /**
     * Total number of checkpointed frames in the log file (including any that were already
     * checkpointed before the function was called) or to -1 if the checkpoint could not run due to
     * an error or because the database is not in WAL mode.
     */
    public val checkpointedFrameCount: Int
}