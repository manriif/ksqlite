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
package ksqlite.types.internal

import ksqlite.types.SqliteCompleteResult
import ksqlite.types.SqliteDbReadonlyResult
import ksqlite.types.SqliteResultCode

/**
 * Converts [value] to [SqliteCompleteResult].
 */
public fun convertCompleteResult(value: Int): SqliteCompleteResult = when (value) {
    0 -> Incomplete
    1 -> Complete
    else -> SqliteCompleteResult
        .Failure(checkNotNull(convertResultCode(value) as? SqliteResultCode.Failure))
}

/**
 * Converts [value] to [SqliteDbReadonlyResult].
 */
public fun convertDbReadonlyResult(value: Int): SqliteDbReadonlyResult = when (value) {
    0 -> READWRITE
    1 -> READONLY
    -1 -> UNKNOWN_DATABASE
    else -> error("Unexpected result from sqlite3_db_readonly(): $value")
}