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
package ksqlite.kapi.config

import ksqlite.kapi.connection.DatabaseConnection
import ksqlite.types.SqliteSqlLogEvent

/**
 * Receives the lifecycle events of every [DatabaseConnection] opened during the SQLite session,
 * once registered through [ksqlite.kapi.config.ConfigurationScope.setSqlLogger]: opening,
 * executing a statement and closing, see [SqliteSqlLogEvent].
 */
public fun interface SqlLogger {

    /**
     * Called for each [event] of [connection].
     */
    public fun log(
        connection: DatabaseConnection,
        event: SqliteSqlLogEvent
    )
}