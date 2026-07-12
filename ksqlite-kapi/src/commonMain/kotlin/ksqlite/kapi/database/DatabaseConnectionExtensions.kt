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

import kotlin.time.Duration

/**
 * Sets a [BusyHandler] that sleeps for a specified amount of time when a table is locked.
 * Any [BusyHandler] previously passed to [DatabaseConnection.setBusyHandler] is replaced.
 *
 * The [duration] is coerced to [Int.MAX_VALUE] milliseconds.
 *
 * @throws ksqlite.kapi.SQLiteException if setting the timeout fails.
 */
public fun DatabaseConnection.setBusyTimeout(duration: Duration): Unit =
    setBusyTimeout(duration.inWholeMilliseconds.coerceAtMost(Int.MAX_VALUE.toLong()).toInt())