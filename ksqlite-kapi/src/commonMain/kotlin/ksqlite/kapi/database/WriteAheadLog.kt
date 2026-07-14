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
package ksqlite.kapi.database

import ksqlite.types.SqliteCheckpointMode

/**
 * Exposes the Write-Ahead Log APIs of a database connection.
 */
public interface WriteAheadLog {

    /**
     * Causes any database on the connection to automatically checkpoint after committing a
     * transaction if there are [frameCount] or more frames in the write-ahead log file.
     * Any [WriteAheadLogHook] previously passed to [setHook] is replaced.
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurs.
     */
    public fun autoCheckpoint(frameCount: Int)

    /**
     * Runs a checkpoint operation on [database] of the connection in the given [mode].
     *
     * @throws ksqlite.kapi.SQLiteException if an error occurs.
     */
    public fun checkpoint(
        mode: SqliteCheckpointMode = SqliteCheckpointMode.PASSIVE,
        database: String? = null
    ): WriteAheadLogCheckpointResult

    /**
     * Sets the callback that is invoked each time data is committed to a database in wal mode.
     *
     * @throws ksqlite.kapi.SQLiteException if setting the hook fails.
     */
    public fun setHook(hook: WriteAheadLogHook?)
}