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
 * Result for `sqlite3_complete()`.
 *
 * [Determine If An SQL Statement Is Complete](https://sqlite.org/c3ref/complete.html).
 */
public sealed interface SqliteCompleteResult {

    /**
     * The input string appears to be a complete SQL statement.
     */
    public data object Complete : SqliteCompleteResult

    /**
     * The statement is incomplete.
     */
    public data object Incomplete : SqliteCompleteResult

    /**
     * A failure occurred.
     */
    public data class Failure(public val result: SqliteResultCode.Failure) : SqliteCompleteResult
}