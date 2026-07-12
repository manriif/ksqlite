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

import ksqlite.types.SqliteActionCode

/**
 * Returns all [SqliteActionCode]s.
 */
private fun sqliteActionCodes(): Set<SqliteActionCode> = setOf(
    SqliteActionCode.CREATE_INDEX,
    SqliteActionCode.CREATE_TABLE,
    SqliteActionCode.CREATE_TEMP_INDEX,
    SqliteActionCode.CREATE_TEMP_TABLE,
    SqliteActionCode.CREATE_TEMP_TRIGGER,
    SqliteActionCode.CREATE_TEMP_VIEW,
    SqliteActionCode.CREATE_TRIGGER,
    SqliteActionCode.CREATE_VIEW,
    SqliteActionCode.DELETE,
    SqliteActionCode.DROP_INDEX,
    SqliteActionCode.DROP_TABLE,
    SqliteActionCode.DROP_TEMP_INDEX,
    SqliteActionCode.DROP_TEMP_TABLE,
    SqliteActionCode.DROP_TEMP_TRIGGER,
    SqliteActionCode.DROP_TEMP_VIEW,
    SqliteActionCode.DROP_TRIGGER,
    SqliteActionCode.DROP_VIEW,
    SqliteActionCode.INSERT,
    SqliteActionCode.PRAGMA,
    SqliteActionCode.READ,
    SqliteActionCode.SELECT,
    SqliteActionCode.TRANSACTION,
    SqliteActionCode.UPDATE,
    SqliteActionCode.ATTACH,
    SqliteActionCode.DETACH,
    SqliteActionCode.ALTER_TABLE,
    SqliteActionCode.REINDEX,
    SqliteActionCode.ANALYZE,
    SqliteActionCode.CREATE_VTABLE,
    SqliteActionCode.DROP_VTABLE,
    SqliteActionCode.FUNCTION,
    SqliteActionCode.SAVEPOINT,
    SqliteActionCode.COPY,
    SqliteActionCode.RECURSIVE
)

/**
 * [SqliteActionCode]s associated by their integer code.
 */
@PublishedApi
internal val SqliteActionCodeMap: Map<Int, SqliteActionCode> =
    sqliteActionCodes().associateBy(SqliteActionCode::code)

/**
 * Converts [code] to [SqliteActionCode].
 */
public inline fun <reified A : SqliteActionCode> convertActionCode(code: Int): A {
    val actionCode = checkNotNull(SqliteActionCodeMap[code]) {
        "Unknown SQLite action code $code"
    }

    check(actionCode is A) { "Unexpected action code: $actionCode" }
    return actionCode
}