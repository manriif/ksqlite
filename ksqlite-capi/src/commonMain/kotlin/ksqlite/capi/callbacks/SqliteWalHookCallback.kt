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
package ksqlite.capi.callbacks

import ksqlite.capi.sqlite3
import ksqlite.types.SqliteResultCode

/**
 * Callback to use with [ksqlite.capi.sqlite3_wal_hook].
 */
public fun interface SqliteWalHookCallback<AppData> {

    /**
     * Details on parameters and result can be found [here](https://sqlite.org/c3ref/wal_hook.html).
     */
    public fun apply(
        appData: AppData,
        db: sqlite3,
        databaseName: String,
        pageCount: Int
    ): SqliteResultCode.OkOrFailure
}