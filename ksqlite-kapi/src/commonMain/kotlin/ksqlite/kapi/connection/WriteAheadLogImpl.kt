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

import ksqlite.capi.memory.Int32OutputParam
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_wal_autocheckpoint
import ksqlite.capi.sqlite3_wal_checkpoint_v2
import ksqlite.capi.sqlite3_wal_hook
import ksqlite.internal.runtime.closeable.CloseableScope
import ksqlite.kapi.helpers.resultCheck
import ksqlite.kapi.helpers.sqliteResultCheck
import ksqlite.kapi.helpers.usingParams
import ksqlite.types.SqliteCheckpointMode

internal class WriteAheadLogImpl(
    private val db: sqlite3,
    private val scope: CloseableScope
) : WriteAheadLog {

    override fun autoCheckpoint(frameCount: Int) =
        scope.notClosed { sqliteResultCheck(sqlite3_wal_autocheckpoint(db, frameCount)) }

    override fun checkpoint(
        mode: SqliteCheckpointMode,
        database: String?
    ): WriteAheadLogCheckpointResult = scope.notClosed {
        usingParams(
            param1 = Int32OutputParam(0),
            param2 = Int32OutputParam(0),
            transform = ::WriteAheadLogCheckpointResultImpl
        ) { outNLog, outNCkpt ->
            db.resultCheck(sqlite3_wal_checkpoint_v2(db, database, mode, outNLog, outNCkpt))
        }
    }

    override fun setHook(hook: WriteAheadLogHook?) = scope.notClosed {
        if (hook != null) {
            sqlite3_wal_hook(db, hook, WriteAheadLogHookCallback)
        } else {
            sqlite3_wal_hook(db, null, null)
        }
    }
}