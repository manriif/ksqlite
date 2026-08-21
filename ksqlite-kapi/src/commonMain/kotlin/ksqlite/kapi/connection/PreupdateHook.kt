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

import ksqlite.types.SqliteActionCode

/**
 * Callback to use with [DatabaseConnection.setPreupdateHook].
 */
public fun interface PreupdateHook {

    /**
     * Called before a row is inserted, updated or deleted in [tableName] of [databaseName] on
     * [connection]. [action] identifies the kind of change. [oldRowid] is the rowid of the row
     * before the change and [newRowid] the rowid it will have afterward, both are only meaningful
     * for the [action] they apply to. The receiver [PreupdateHookScope] gives access to the old
     * and new column values.
     */
    public fun PreupdateHookScope.apply(
        connection: DatabaseConnection,
        action: SqliteActionCode.RowChange,
        databaseName: String,
        tableName: String,
        oldRowid: Long,
        newRowid: Long
    )
}