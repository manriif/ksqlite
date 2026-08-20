/*
 * Copyright (C) 2026 Maanrifa Bacar Ali
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ksqlite.kapi.snapshot

import ksqlite.capi.sqlite3_snapshot
import ksqlite.capi.sqlite3_snapshot_cmp
import ksqlite.capi.sqlite3_snapshot_free
import ksqlite.internal.runtime.closeable.UnsafeCloseableScope

internal class SnapshotImpl(val snapshot: sqlite3_snapshot) :
    Snapshot,
    UnsafeCloseableScope() {

    override fun compareTo(other: Snapshot): Int {
        val otherImpl = other.impl

        ensureNotClosed { "Snapshot is closed" }
        otherImpl.ensureNotClosed { "Snapshot to compare to is closed" }

        return sqlite3_snapshot_cmp(snapshot, otherImpl.snapshot)
    }

    override fun onClose() {
        sqlite3_snapshot_free(snapshot)
    }
}

/**
 * Returns the [Snapshot] implementation.
 */
internal val Snapshot.impl: SnapshotImpl
    get() = when (this) {
        is SnapshotImpl -> this
    }