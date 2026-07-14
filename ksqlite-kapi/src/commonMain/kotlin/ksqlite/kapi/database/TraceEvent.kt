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
package ksqlite.kapi.database

import ksqlite.kapi.statement.PreparedStatement
import ksqlite.types.SqliteTraceEventCode

/**
 * Event passed as parameter to [Trace].
 */
public sealed interface TraceEvent {

    /**
     * [SqliteTraceEventCode.STMT] related event.
     */
    public class Stmt(
        public val statement: PreparedStatement,
        public val sql: String
    ) : TraceEvent

    /**
     * [SqliteTraceEventCode.PROFILE] related event.
     */
    public class Profile(
        public val statement: PreparedStatement,
        public val nanos: Long
    ) : TraceEvent

    /**
     * [SqliteTraceEventCode.ROW] related event.
     */
    public class Row(public val statement: PreparedStatement) : TraceEvent

    /**
     * [SqliteTraceEventCode.CLOSE] related event.
     */
    public class Close(public val connection: DatabaseConnection) : TraceEvent
}