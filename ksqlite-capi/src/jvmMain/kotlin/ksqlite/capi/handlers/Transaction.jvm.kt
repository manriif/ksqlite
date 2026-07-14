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
package ksqlite.capi.handlers

import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteRollbackHookCallback
import ksqlite.foreign.`sqlite3_commit_hook$x0`
import ksqlite.foreign.`sqlite3_rollback_hook$x0`
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler :
    Handler(),
    `sqlite3_commit_hook$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_commit_hook$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment
    ): Int = handle(refPointer) { callback: SqliteCommitHookCallback<Any?>, appData ->
        callback.apply(appData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler :
    Handler(),
    `sqlite3_rollback_hook$x0`.Function {

    override fun allocate(arena: Arena): MemorySegment =
        `sqlite3_rollback_hook$x0`.allocate(this, arena)

    override fun apply(
        refPointer: MemorySegment
    ): Unit = handle(refPointer) { callback: SqliteRollbackHookCallback<Any?>, appData ->
        callback.apply(appData)
    }
}