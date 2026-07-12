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

import ksqlite.types.SqliteTraceEventCode
import ksqlite.types.SqliteTransactionState

/**
 * Returns all constants [SqliteTraceEventCode.Constant]s.
 */
private fun sqliteTraceConstants(): Set<SqliteTraceEventCode.Constant> = setOf(
    SqliteTraceEventCode.STMT,
    SqliteTraceEventCode.PROFILE,
    SqliteTraceEventCode.ROW,
    SqliteTraceEventCode.CLOSE
)

/**
 * [ksqlite.types.SqliteTraceEventCode.Constant]s associated by their code value.
 */
private val TraceConstantMap = sqliteTraceConstants().associateBy(SqliteTraceEventCode::value)

/**
 * Converts [code] to [SqliteTransactionState].
 */
public fun convertTraceCode(code: Int): SqliteTraceEventCode.Constant =
    checkNotNull(TraceConstantMap[code]) { "Unknown SQLite trace code: $code" }