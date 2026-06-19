package ksqlite.kapi.database

internal data class WriteAheadLogCheckpointResultImpl(
    override val frameCount: Int,
    override val checkpointedFrameCount: Int
) : WriteAheadLogCheckpointResult