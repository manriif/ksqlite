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

import ksqlite.capi.callbacks.SqlitePreupdateHookCallback
import ksqlite.capi.callbacks.SqliteUpdateHookCallback
import ksqlite.capi.sqlite3
import ksqlite.foreign.callbacks.PreupdateHookCallback
import ksqlite.foreign.callbacks.UpdateHookCallback
import ksqlite.types.internal.convertActionCode

/**
 * Handler for [ksqlite.capi.sqlite3_preupdate_hook].
 */
internal class PreupdateHookHandler<AppData> :
    Handler<SqlitePreupdateHookCallback<AppData>, AppData>(),
    PreupdateHookCallback {

    override fun apply(
        db: Long,
        op: Int,
        dbName: String,
        dbTable: String,
        iKey1: Long,
        iKey2: Long
    ) = handle { callback, appData ->
        callback.apply(
            appData = appData,
            db = sqlite3(db),
            action = convertActionCode(op),
            dbName = dbName,
            tableName = dbTable,
            oldRowid = iKey1,
            newRowid = iKey2
        )
    }
}

/**
 * Handler for [ksqlite.capi.sqlite3_update_hook].
 */
internal class UpdateHookHandler<AppData> :
    Handler<SqliteUpdateHookCallback<AppData>, AppData>(),
    UpdateHookCallback {

    override fun apply(
        opId: Int,
        dbName: String,
        tableName: String,
        rowId: Long
    ) = handle { callback, appData ->
        callback.apply(
            appData = appData,
            action = convertActionCode(opId),
            dbName = dbName,
            tableName = tableName,
            rowid = rowId
        )
    }
}