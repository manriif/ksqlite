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

/**
 * An immutable, point-in-time snapshot of the state of a WAL mode database, returned by
 * [ksqlite.kapi.connection.DatabaseConnection.createSnapshot] and consumed by
 * [ksqlite.kapi.connection.DatabaseConnection.openSnapshot].
 *
 * Unless documented otherwise, every member throws [IllegalStateException] once this snapshot
 * is closed.
 */
public sealed interface Snapshot :
    Comparable<Snapshot>,
    AutoCloseable {

    /**
     * Compares the age of this snapshot to [other]. A negative number means this snapshot is
     * older than [other], zero that they refer to the same point in time, and a positive number
     * that this snapshot is newer. Comparing snapshots taken from different databases gives an
     * unspecified result.
     */
    abstract override fun compareTo(other: Snapshot): Int

    /**
     * Destroys this snapshot. Calling this again on an already closed snapshot has no effect.
     */
    abstract override fun close()
}