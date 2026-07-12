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
package ksqlite.kapi.helpers

import ksqlite.capi.sqlite3_log
import ksqlite.types.SqliteResultCode

/**
 * Logs [message] using SQLite logging API.
 * SQLite must have been initialized.
 */
internal fun ksqliteLog(
    message: String,
    resultCode: SqliteResultCode.Failure = SqliteResultCode.ERROR
) {
    sqlite3_log(resultCode.code, message)
}

/**
 * Logs [throwable] (unexpected) using SQLite logging API.
 * SQLite must have been initialized.
 */
internal fun ksqliteLog(
    throwable: Throwable,
    resultCode: SqliteResultCode.Failure = SqliteResultCode.MISUSE
) = ksqliteLog(
    message = "Uncaught unexpected exception\n${throwable.stackTraceToString()}",
    resultCode = resultCode
)