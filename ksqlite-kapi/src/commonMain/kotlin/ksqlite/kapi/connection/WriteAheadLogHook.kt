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

import ksqlite.kapi.SQLiteException

/**
 * Callback to use with [WriteAheadLog.setHook].
 */
public fun interface WriteAheadLogHook {

    /**
     * Called each time data is committed to [databaseName] of [connection] in WAL mode.
     * [pageCount] is the number of frames the write-ahead log file now holds.
     *
     * If an error is detected, an [SQLiteException] must be thrown.
     */
    public fun apply(
        connection: DatabaseConnection,
        databaseName: String,
        pageCount: Int
    )
}