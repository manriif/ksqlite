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
@file:Suppress("ClassName")

package ksqlite.capi.types

import ksqlite.capi.callbacks.SqliteTraceCallback
import ksqlite.capi.sqlite3
import ksqlite.capi.sqlite3_stmt
import ksqlite.types.SqliteTraceEventCode

/**
 * Event passed as parameter to [SqliteTraceCallback].
 */
public sealed interface SqliteTraceEvent {

    /**
     * [SqliteTraceEventCode.STMT] related event.
     */
    public class Stmt(
        public val stmt: sqlite3_stmt,
        public val sql: String
    ) : SqliteTraceEvent

    /**
     * [SqliteTraceEventCode.PROFILE] related event.
     */
    public class Profile(
        public val stmt: sqlite3_stmt,
        public val nanos: Long
    ) : SqliteTraceEvent

    /**
     * [SqliteTraceEventCode.ROW] related event.
     */
    public class Row(public val stmt: sqlite3_stmt) : SqliteTraceEvent

    /**
     * [SqliteTraceEventCode.CLOSE] related event.
     */
    public class Close(public val db: sqlite3) : SqliteTraceEvent
}