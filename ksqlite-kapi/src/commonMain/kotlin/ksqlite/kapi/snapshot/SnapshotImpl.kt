package ksqlite.kapi.snapshot

import ksqlite.capi.sqlite3_snapshot_cmp
import ksqlite.capi.sqlite3_snapshot_free
import ksqlite.capi.types.sqlite3_snapshot
import ksqlite.kapi.helpers.DelegatingCloseableScope

internal class SnapshotImpl(override val snapshot: sqlite3_snapshot) : Snapshot() {

    override val scope = DelegatingCloseableScope {
        sqlite3_snapshot_free(snapshot)
    }

    override fun compareTo(other: Snapshot): Int {
        scope.ensureNotClosed { "Snapshot is closed" }
        other.scope.ensureNotClosed { "Snapshot to compare to is closed" }
        return sqlite3_snapshot_cmp(snapshot, other.snapshot)
    }

    override fun close() = scope.close()
}