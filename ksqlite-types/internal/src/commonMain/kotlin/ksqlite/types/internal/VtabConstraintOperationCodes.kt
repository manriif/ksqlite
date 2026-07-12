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

import ksqlite.types.vtab.SqliteVtabConstraintOperatorCode

/**
 * Returns all [SqliteVtabConstraintOperatorCode]s except custom.
 */
private fun sqliteVtabConstraintOperatorCodes(): Set<SqliteVtabConstraintOperatorCode> = setOf(
    SqliteVtabConstraintOperatorCode.EQ,
    SqliteVtabConstraintOperatorCode.GT,
    SqliteVtabConstraintOperatorCode.LE,
    SqliteVtabConstraintOperatorCode.LT,
    SqliteVtabConstraintOperatorCode.GE,
    SqliteVtabConstraintOperatorCode.MATCH,
    SqliteVtabConstraintOperatorCode.LIKE,
    SqliteVtabConstraintOperatorCode.GLOB,
    SqliteVtabConstraintOperatorCode.REGEXP,
    SqliteVtabConstraintOperatorCode.NE,
    SqliteVtabConstraintOperatorCode.ISNOT,
    SqliteVtabConstraintOperatorCode.ISNOTNULL,
    SqliteVtabConstraintOperatorCode.ISNULL,
    SqliteVtabConstraintOperatorCode.IS,
    SqliteVtabConstraintOperatorCode.LIMIT,
    SqliteVtabConstraintOperatorCode.OFFSET,
    SqliteVtabConstraintOperatorCode.FUNCTION
)

/**
 * [SqliteVtabConstraintOperatorCode]s associated by their integer code.
 */
private val SqliteVtabConstraintOperatorCodeMap =
    sqliteVtabConstraintOperatorCodes().associateBy(SqliteVtabConstraintOperatorCode::code)

/**
 * Converts [code] to [SqliteVtabConstraintOperatorCode].
 */
public fun convertVtabConstraintOperatorCode(code: Int): SqliteVtabConstraintOperatorCode =
    SqliteVtabConstraintOperatorCodeMap[code] ?: SqliteVtabConstraintOperatorCode.Custom(code)