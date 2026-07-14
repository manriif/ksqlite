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
 * Result for `sqlite3_db_readonly()`.
 *
 * [Determine if a database is read-only](https://sqlite.org/c3ref/db_readonly.html).
 */
public enum class SqliteDbReadonlyResult {

    /**
     * The database is in read/write mode.
     */
    READWRITE,

    /**
     * The database is readonly.
     */
    READONLY,

    /**
     * The database is not part of the connection.
     */
    UNKNOWN_DATABASE
}