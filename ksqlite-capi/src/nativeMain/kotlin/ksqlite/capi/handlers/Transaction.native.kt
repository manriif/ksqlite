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

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.staticCFunction
import ksqlite.capi.callbacks.SqliteCommitHookCallback
import ksqlite.capi.callbacks.SqliteRollbackHookCallback

///////////////////////////////////////////////////////////////////////////
// Commit
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [commitHookHandler].
 */
internal val CommitHookHandler = staticCFunction(::commitHookHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
private fun commitHookHandler(
    refPointer: COpaquePointer?
) = handle(refPointer) { callback: SqliteCommitHookCallback<Any?>, appData ->
    callback.apply(appData)
}

///////////////////////////////////////////////////////////////////////////
// Rollback
///////////////////////////////////////////////////////////////////////////

/**
 * Static C function for [rollbackHookHandler].
 */
internal val RollbackHookHandler = staticCFunction(::rollbackHookHandler)

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
private fun rollbackHookHandler(
    refPointer: COpaquePointer?
) = handle(refPointer) { callback: SqliteRollbackHookCallback<Any?>, appData ->
    callback.apply(appData)
}