package ksqlite.kapi.snapshot

import ksqlite.capi.sqlite3_snapshot
import ksqlite.kapi.helpers.ClosableScope

public abstract class Snapshot internal constructor() : Comparable<Snapshot>, AutoCloseable {

    internal abstract val snapshot: sqlite3_snapshot
    internal abstract val scope: ClosableScope

    /**
     * Compares the ages of two valid snapshot handles.
     */
    abstract override fun compareTo(other: Snapshot): Int

    /**
     * Destroys this snapshot.
     */
    abstract override fun close()
}