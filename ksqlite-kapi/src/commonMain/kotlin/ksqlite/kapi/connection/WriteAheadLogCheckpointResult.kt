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
package ksqlite.kapi.connection

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