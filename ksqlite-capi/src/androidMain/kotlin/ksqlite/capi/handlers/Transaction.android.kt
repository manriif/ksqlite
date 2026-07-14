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
import ksqlite.foreign.callbacks.CommitHookCallback
import ksqlite.foreign.callbacks.RollbackHookCallback

/**
 * Handler for [ksqlite.capi.sqlite3_commit_hook].
 */
internal class CommitHookHandler<AppData> :
    Handler<SqliteCommitHookCallback<AppData>, AppData>(),
    CommitHookCallback {

    override fun apply(): Int = handle { callback, appData ->
        callback.apply(appData)
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_rollback_hook].
 */
internal class RollbackHookHandler<AppData> :
    Handler<SqliteRollbackHookCallback<AppData>, AppData>(),
    RollbackHookCallback {

    override fun apply() = handle { callback, appData ->
        callback.apply(appData)
    }
}