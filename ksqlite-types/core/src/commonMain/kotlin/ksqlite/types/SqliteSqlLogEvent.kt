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
package ksqlite.types

/**
 * Event passed as third parameter to an SQLLOG callback.
 */
public sealed interface SqliteSqlLogEvent {

    /**
     * Database connection has just been opened.
     */
    public class DatabaseOpened(public val dbFileName: String) : SqliteSqlLogEvent

    /**
     * Statement has just been executed.
     */
    public class StatementExecuted(public val statement: String) : SqliteSqlLogEvent

    /**
     * Database connection is being closed.
     */
    public object DatabaseClosed : SqliteSqlLogEvent
}