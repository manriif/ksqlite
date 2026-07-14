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
@file:Suppress("SpellCheckingInspection")

package ksqlite.types.vtab

/**
 * These macros define the allowed values for the sqlite3_index_info.aConstraint[].op field. Each
 * value represents an operator that is part of a constraint term in the WHERE clause of a query
 * that uses a virtual table.
 *
 * [Virtual Table Constraint Operator Codes](https://sqlite.org/c3ref/c_index_constraint_eq.html).
 */
public sealed class SqliteVtabConstraintOperatorCode(public open val code: Int) {

    public data object EQ : SqliteVtabConstraintOperatorCode(2)

    public data object GT : SqliteVtabConstraintOperatorCode(4)

    public data object LE : SqliteVtabConstraintOperatorCode(8)

    public data object LT : SqliteVtabConstraintOperatorCode(16)

    public data object GE : SqliteVtabConstraintOperatorCode(32)

    public data object MATCH : SqliteVtabConstraintOperatorCode(64)

    public data object LIKE : SqliteVtabConstraintOperatorCode(65)

    public data object GLOB : SqliteVtabConstraintOperatorCode(66)

    public data object REGEXP : SqliteVtabConstraintOperatorCode(67)

    public data object NE : SqliteVtabConstraintOperatorCode(68)

    public data object ISNOT : SqliteVtabConstraintOperatorCode(69)

    public data object ISNOTNULL : SqliteVtabConstraintOperatorCode(70)

    public data object ISNULL : SqliteVtabConstraintOperatorCode(71)

    public data object IS : SqliteVtabConstraintOperatorCode(72)

    public data object LIMIT : SqliteVtabConstraintOperatorCode(73)

    public data object OFFSET : SqliteVtabConstraintOperatorCode(74)

    public data object FUNCTION : SqliteVtabConstraintOperatorCode(150)

    /**
     * Custom operator code returned by xFindFunction callback.
     */
    public data class Custom(override val code: Int) : SqliteVtabConstraintOperatorCode(code) {
        init {
            require(code >= FUNCTION.code) {
                "Custom constraint code must be greater than or equals to ${FUNCTION.code}"
            }
        }
    }
}