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

import ksqlite.types.SqliteTransactionState

/**
 * [SqliteTransactionState]s associated by their integer id.
 */
private val SqliteTransactionStateMap =
    SqliteTransactionState.entries.associateBy(SqliteTransactionState::value)

/**
 * Converts [state] to [SqliteTransactionState].
 */
public fun convertTransactionState(state: Int): SqliteTransactionState? {
    if (state == -1) {
        return null
    }

    return checkNotNull(SqliteTransactionStateMap[state]) {
        "Unknown SQLite transaction state: $state"
    }
}